@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package com.michaldrabik.ui_repository

import androidx.room.withTransaction
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.network.Cloud
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.User
import com.michaldrabik.ui_model.error.TraktAuthError
import javax.inject.Inject
import com.michaldrabik.network.trakt.model.User as UserModel

@AppScope
class UserTraktManager @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase
) {

  companion object {
    private const val TRAKT_TOKEN_EXPIRATION_MS = 5_184_000_000 // 2 months
  }

  private var traktUsername = ""
  private var traktToken: TraktAuthToken? = null
  private var traktRefreshToken: TraktRefreshToken? = null
  private var traktTokenTimestamp = 0L

  suspend fun checkAuthorization(): TraktAuthToken {
    if (!isAuthorized()) {
      if (traktRefreshToken == null) throw TraktAuthError("Authorization needed")
      val tokens = cloud.traktApi.refreshAuthTokens(traktRefreshToken?.token!!)
      val user = cloud.traktApi.fetchMyProfile(tokens.access_token)
      saveToken(tokens.access_token, tokens.refresh_token, user)
    }
    return traktToken!!
  }

  suspend fun authorize(authCode: String) {
    val tokens = cloud.traktApi.fetchAuthTokens(authCode)
    try {
      val user = cloud.traktApi.fetchMyProfile(tokens.access_token)
      saveToken(tokens.access_token, tokens.refresh_token, user)
    } catch (t: Throwable) {
      saveToken(tokens.access_token, tokens.refresh_token, UserModel("", ""))
    }
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
          traktTokenTimestamp = 0,
          traktUsername = ""
        )
      )
    }

    try {
      traktToken?.let { cloud.traktApi.revokeAuthTokens(it.token) }
    } catch (t: Throwable) {
      // NOOP
    } finally {
      traktToken = null
      traktRefreshToken = null
      traktTokenTimestamp = 0
      traktUsername = ""
    }
  }

  suspend fun getTraktUsername(): String {
    val user = database.userDao().get()
    user?.let {
      traktUsername = it.traktUsername
    }
    return traktUsername
  }

  private suspend fun saveToken(token: String, refreshToken: String, userModel: UserModel) {
    val timestamp = nowUtcMillis()
    database.withTransaction {
      val user = database.userDao().get()
      database.userDao().upsert(
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
          tvdbTokenTimestamp = 0
        )
      )
    }
    traktToken = TraktAuthToken(token)
    traktRefreshToken = TraktRefreshToken(refreshToken)
    traktTokenTimestamp = timestamp
    traktUsername = userModel.username
  }
}

inline class TraktAuthToken(val token: String = "")

inline class TraktRefreshToken(val token: String = "")
