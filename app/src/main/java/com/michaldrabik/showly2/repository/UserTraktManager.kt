@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package com.michaldrabik.showly2.repository

import androidx.room.withTransaction
import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.model.error.TraktAuthError
import com.michaldrabik.showly2.utilities.extensions.nowUtcMillis
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.User
import javax.inject.Inject

@AppScope
class UserTraktManager @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase
) {

  companion object {
    private const val TRAKT_TOKEN_EXPIRATION_MS = 5_184_000_000 //2 months
  }

  private var traktToken: TraktAuthToken? = null
  private var traktRefreshToken: TraktRefreshToken? = null
  private var traktTokenTimestamp = 0L

  suspend fun checkAuthorization(): TraktAuthToken {
    if (!isAuthorized()) {
      if (traktRefreshToken == null) throw TraktAuthError("Authorization needed")
      val tokens = cloud.traktApi.refreshAuthTokens(traktRefreshToken?.token!!)
      saveToken(tokens.access_token, tokens.refresh_token)
    }
    return traktToken!!
  }

  suspend fun authorize(authCode: String) {
    val tokens = cloud.traktApi.fetchAuthTokens(authCode)
    saveToken(tokens.access_token, tokens.refresh_token)
  }

  suspend fun isAuthorized(): Boolean {
    if (traktToken == null || traktRefreshToken == null) {
      val user = database.userDao().get()
      user?.let {
        traktToken = TraktAuthToken(it.traktToken)
        traktRefreshToken = TraktRefreshToken(it.traktRefreshToken)
        traktTokenTimestamp = it.traktTokenTimestamp
      }
    }
    return when {
      traktToken?.token.isNullOrEmpty() -> false
      nowUtcMillis() - traktTokenTimestamp > TRAKT_TOKEN_EXPIRATION_MS -> false
      else -> true
    }
  }

  suspend fun revokeToken() {
    database.withTransaction {
      val user = database.userDao().get()!!
      database.userDao().upsert(
        user.copy(
          traktToken = "",
          traktRefreshToken = "",
          traktTokenTimestamp = 0
        )
      )
    }

    try {
      traktToken?.let { cloud.traktApi.revokeAuthTokens(it.token) }
    } catch (t: Throwable) {
      //NOOP
    } finally {
      traktToken = null
      traktRefreshToken = null
      traktTokenTimestamp = 0
    }
  }

  private suspend fun saveToken(token: String, refreshToken: String) {
    val timestamp = nowUtcMillis()
    database.withTransaction {
      val user = database.userDao().get()
      database.userDao().upsert(
        user?.copy(
          traktToken = token,
          traktRefreshToken = refreshToken,
          traktTokenTimestamp = timestamp
        ) ?: User(
          traktToken = token,
          traktRefreshToken = refreshToken,
          traktTokenTimestamp = timestamp,
          tvdbToken = "",
          tvdbTokenTimestamp = 0
        )
      )

    }
    traktToken = TraktAuthToken(token)
    traktRefreshToken = TraktRefreshToken(refreshToken)
    traktTokenTimestamp = timestamp
  }
}

inline class TraktAuthToken(val token: String = "")

inline class TraktRefreshToken(val token: String = "")

