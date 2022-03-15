package com.michaldrabik.data_remote.reddit.api

import com.michaldrabik.data_remote.Config
import com.michaldrabik.data_remote.reddit.model.RedditAuthResponse
import okhttp3.Credentials

internal class RedditAuthApi(private val service: RedditService) {

  suspend fun fetchAuthToken(): RedditAuthResponse {
    val credentials = Credentials.basic(Config.REDDIT_CLIENT_ID, "")
    return service.fetchAccessToken(
      credentials,
      Config.REDDIT_GRANT_TYPE,
      Config.REDDIT_DEVICE_ID
    )
  }
}
