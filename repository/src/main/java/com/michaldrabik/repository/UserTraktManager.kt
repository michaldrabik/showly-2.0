@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package com.michaldrabik.repository

import androidx.room.withTransaction
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_local.database.model.User
import com.michaldrabik.data_remote.Cloud
import com.michaldrabik.ui_model.error.TraktAuthError
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import com.michaldrabik.data_remote.trakt.model.User as UserModel

@Singleton
class UserTraktManager @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
) {

  private val traktTokenExpiration by lazy { TimeUnit.DAYS.toMillis(60) }

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
    val user = cloud.traktApi.fetchMyProfile(tokens.access_token)
    saveToken(tokens.access_token, tokens.refresh_token, user)
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
      nowUtcMillis() - traktTokenTimestamp > traktTokenExpiration -> false
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

  suspend fun getUsername(): String {
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
          tvdbTokenTimestamp = 0,
          redditToken = "",
          redditTokenTimestamp = 0
        )
      )
    }
    traktToken = TraktAuthToken(token)
    traktRefreshToken = TraktRefreshToken(refreshToken)
    traktTokenTimestamp = timestamp
    traktUsername = userModel.username
  }

  suspend fun clearTraktLogs() {
    database.traktSyncLogDao().deleteAll()
  }
}

@JvmInline
value class TraktAuthToken(val token: String = "")

@JvmInline
value class TraktRefreshToken(val token: String = "")
