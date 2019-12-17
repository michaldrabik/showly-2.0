package com.michaldrabik.showly2.repository

import androidx.room.withTransaction
import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.utilities.extensions.nowUtcMillis
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.User
import javax.inject.Inject

@AppScope
class UserTvdbManager @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase
) {

  companion object {
    private const val TVDB_TOKEN_EXPIRATION_MS = 72_000_000 //20 hours
  }

  private var tvdbToken: String? = null
  private var tvdbTokenTimestamp = 0L

  suspend fun checkAuthorization() {
    if (!isAuthorized()) {
      val token = cloud.tvdbApi.authorize()
      saveToken(token.token)
    }
  }

  suspend fun getToken(): String {
    if (tvdbToken.isNullOrEmpty()) {
      tvdbToken = database.userDao().get()?.tvdbToken
    }
    return tvdbToken!!
  }

  private suspend fun saveToken(token: String) {
    val timestamp = nowUtcMillis()
    database.withTransaction {
      val user = database.userDao().get()
      database.userDao().upsert(
        user?.copy(
          tvdbToken = token,
          tvdbTokenTimestamp = timestamp
        ) ?: User(
          tvdbToken = token,
          tvdbTokenTimestamp = timestamp,
          traktToken = "",
          traktRefreshToken = "",
          traktTokenTimestamp = 0,
          traktUsername = ""
        )
      )
    }
    tvdbToken = token
    tvdbTokenTimestamp = timestamp
  }

  private suspend fun isAuthorized(): Boolean {
    if (tvdbToken == null) {
      val user = database.userDao().get()
      user?.let {
        tvdbToken = it.tvdbToken
        tvdbTokenTimestamp = it.tvdbTokenTimestamp
      }
    }
    return when {
      tvdbToken.isNullOrEmpty() -> false
      nowUtcMillis() - tvdbTokenTimestamp > TVDB_TOKEN_EXPIRATION_MS -> false
      else -> true
    }
  }
}