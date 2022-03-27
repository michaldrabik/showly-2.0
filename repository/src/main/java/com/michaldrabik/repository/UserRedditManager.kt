@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package com.michaldrabik.repository

import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_local.database.model.User
import com.michaldrabik.data_local.utilities.TransactionsProvider
import com.michaldrabik.data_remote.RemoteDataSource
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRedditManager @Inject constructor(
  private val remoteSource: RemoteDataSource,
  private val localSource: LocalDataSource,
  private val transactions: TransactionsProvider,
) {

  companion object {
    private const val TOKEN_EXPIRE_BUFFER_SECONDS = 60
  }

  private var redditToken: RedditAuthToken? = null
  private var redditTokenTimestamp: Long = 0

  suspend fun checkAuthorization(): RedditAuthToken {
    if (redditToken != null && nowUtcMillis() < redditTokenTimestamp) {
      return redditToken!!
    }

    val user = localSource.user.get()
    user?.let {
      if (nowUtcMillis() < it.redditTokenTimestamp) {
        val authToken = RedditAuthToken(it.redditToken)
        redditToken = authToken
        redditTokenTimestamp = it.redditTokenTimestamp
        return authToken
      }
    }

    val authResponse = remoteSource.reddit.fetchAuthToken()
    val resultToken = RedditAuthToken(authResponse.access_token)
    val resultTokenTimestamp = nowUtcMillis() + TimeUnit.SECONDS.toMillis(authResponse.expires_in - TOKEN_EXPIRE_BUFFER_SECONDS)

    transactions.withTransaction {
      val userDb = localSource.user.get()
      localSource.user.upsert(
        userDb?.copy(
          redditToken = resultToken.token,
          redditTokenTimestamp = resultTokenTimestamp,
        ) ?: User(
          redditToken = resultToken.token,
          redditTokenTimestamp = resultTokenTimestamp,
          traktToken = "",
          traktRefreshToken = "",
          traktTokenTimestamp = 0,
          traktUsername = "",
          tvdbToken = "",
          tvdbTokenTimestamp = 0,
        )
      )
    }

    redditToken = resultToken
    redditTokenTimestamp = resultTokenTimestamp

    return resultToken
  }
}

@JvmInline
value class RedditAuthToken(val token: String = "")
