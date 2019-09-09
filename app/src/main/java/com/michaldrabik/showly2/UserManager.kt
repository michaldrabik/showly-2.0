package com.michaldrabik.showly2

import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.User
import java.lang.System.currentTimeMillis
import javax.inject.Inject

@AppScope
class UserManager @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase
) {

  companion object {
    private const val TVDB_TOKEN_EXPIRATION_MS = 72_000_000 //20 hours
  }

  private var tvdbToken: String? = null
  private var tvdbTokenTimestamp = 0L

  suspend fun checkAuthorization() {
    if (!isTvdbAuthorized()) {
      val token = cloud.tvdbApi.authorize()
      saveTvdbToken(token.token)
    }
  }

  suspend fun getTvdbToken(): String {
    if (tvdbToken.isNullOrEmpty()) {
      tvdbToken = database.userDao().get()?.tvdbToken
    }
    return tvdbToken!!
  }

  private suspend fun saveTvdbToken(token: String) {
    val timestamp = currentTimeMillis()
    database.userDao().upsert(User(tvdbToken = token, tvdbTokenTimestamp = timestamp))
    tvdbToken = token
    tvdbTokenTimestamp = timestamp
  }

  private suspend fun isTvdbAuthorized(): Boolean {
    if (tvdbToken == null) {
      val user = database.userDao().get()
      user?.let {
        tvdbToken = it.tvdbToken
        tvdbTokenTimestamp = it.tvdbTokenTimestamp
      }
    }
    return when {
      tvdbToken.isNullOrEmpty() -> false
      currentTimeMillis() - tvdbTokenTimestamp > TVDB_TOKEN_EXPIRATION_MS -> false
      else -> true
    }
  }
}