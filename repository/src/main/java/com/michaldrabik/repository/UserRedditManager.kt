@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package com.michaldrabik.repository

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_remote.Cloud
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AppScope
class UserRedditManager @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
) {

  private var redditToken: RedditAuthToken? = null
  private var redditTokenTimestamp: Long = 0

  suspend fun checkAuthorization(): RedditAuthToken {
    if (redditToken != null && nowUtcMillis() < redditTokenTimestamp) {
      return redditToken!!
    }

    val authResponse = cloud.redditApi.fetchAuthToken()
    val resultToken = RedditAuthToken(authResponse.access_token)

    redditToken = resultToken
    redditTokenTimestamp = nowUtcMillis() + TimeUnit.SECONDS.toMillis(authResponse.expires_in - 60)
    return resultToken
  }
}

inline class RedditAuthToken(val token: String = "")

