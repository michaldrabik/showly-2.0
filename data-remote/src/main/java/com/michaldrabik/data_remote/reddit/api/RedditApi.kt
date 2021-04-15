package com.michaldrabik.data_remote.reddit.api

import com.michaldrabik.data_remote.Config
import com.michaldrabik.data_remote.di.CloudScope
import javax.inject.Inject

@CloudScope
class RedditApi @Inject constructor(
  private val authApi: RedditAuthApi,
  private val listingApi: RedditListingApi,
) {

  suspend fun fetchAuthToken() = authApi.fetchAuthToken()

  suspend fun fetchTelevision(
    token: String,
    limit: Int = Config.REDDIT_LIST_LIMIT,
  ) = listingApi.fetchTelevision(token, limit)

  suspend fun fetchMovies(
    token: String,
    limit: Int = Config.REDDIT_LIST_LIMIT,
  ) = listingApi.fetchMovies(token, limit)
}
