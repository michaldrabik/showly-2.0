@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package com.michaldrabik.repository

import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.data_local.database.model.User
import com.michaldrabik.data_local.sources.UserLocalDataSource
import com.michaldrabik.data_local.utilities.TransactionsProvider
import com.michaldrabik.data_remote.trakt.TraktRemoteDataSource
import com.michaldrabik.ui_model.error.TraktAuthError
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import com.michaldrabik.data_remote.trakt.model.User as UserModel

@Singleton
class UserTraktManager @Inject constructor(
  private val remoteSource: TraktRemoteDataSource,
  private val userLocalSource: UserLocalDataSource,
  private val transactions: TransactionsProvider,
) {

  private val traktTokenExpiration by lazy { TimeUnit.DAYS.toMillis(60) }

  private var traktToken: TraktAuthToken? = null
  private var traktRefreshToken: TraktRefreshToken? = null
  private var traktTokenTimestamp = 0L

  suspend fun checkAuthorization(): TraktAuthToken {
    if (isAuthorized()) return traktToken!!
    if (traktRefreshToken == null) {
      throw TraktAuthError("Authorization needed. Refresh token is null.")
    }
    try {
      val tokens = remoteSource.refreshAuthTokens(traktRefreshToken?.token!!)
      val user = remoteSource.fetchMyProfile(tokens.access_token)
      saveToken(tokens.access_token, tokens.refresh_token, user)
      return traktToken!!
    } catch (error: Throwable) {
      revokeToken()
      throw TraktAuthError("Authorization needed. Refreshing token failed: ${error.message}")
    }
  }

  suspend fun authorize(authCode: String) {
    val tokens = remoteSource.fetchAuthTokens(authCode)
    val user = remoteSource.fetchMyProfile(tokens.access_token)
    saveToken(tokens.access_token, tokens.refresh_token, user)
  }

  suspend fun isAuthorized(): Boolean {
    if (traktToken == null || traktRefreshToken == null) {
      val user = userLocalSource.get()
      user?.let {
        traktToken = TraktAuthToken(it.traktToken)
        traktRefreshToken = TraktRefreshToken(it.traktRefreshToken)
        traktTokenTimestamp = it.traktTokenTimestamp
      }
    }
    return when {
      traktToken?.token.isNullOrEmpty() -> false
      nowUtcMillis() - traktTokenTimestamp > traktTokenExpiration -> false
      else -> true
    }
  }

  suspend fun revokeToken() {
    transactions.withTransaction {
      val user = userLocalSource.get()!!
      val userEntity = user.copy(
        traktToken = "",
        traktRefreshToken = "",
        traktTokenTimestamp = 0,
        traktUsername = ""
      )
      userLocalSource.upsert(userEntity)
    }
    try {
      traktToken?.let { remoteSource.revokeAuthTokens(it.token) }
    } catch (error: Throwable) {
      // Just log error as nothing bad really happens in case of error.
      Timber.w("revokeToken(): $error")
    } finally {
      traktToken = null
      traktRefreshToken = null
      traktTokenTimestamp = 0
    }
  }

  suspend fun getUsername() = userLocalSource.get()?.traktUsername ?: ""

  private suspend fun saveToken(token: String, refreshToken: String, userModel: UserModel) {
    val timestamp = nowUtcMillis()
    transactions.withTransaction {
      val user = userLocalSource.get()
      userLocalSource.upsert(
        user?.copy(
          traktToken = token,
          traktRefreshToken = refreshToken,
          traktTokenTimestamp = timestamp,
          traktUsername = userModel.username
        ) ?: User(
          traktToken = token,
          traktRefreshToken = refreshToken,
          traktTokenTimestamp = timestamp,
          traktUsername = userModel.username,
          tvdbToken = "",
          tvdbTokenTimestamp = 0,
          redditToken = "",
          redditTokenTimestamp = 0
        )
      )
    }
    traktToken = TraktAuthToken(token)
    traktRefreshToken = TraktRefreshToken(refreshToken)
    traktTokenTimestamp = timestamp
  }
}

@JvmInline
value class TraktAuthToken(val token: String = "")

@JvmInline
value class TraktRefreshToken(val token: String = "")
