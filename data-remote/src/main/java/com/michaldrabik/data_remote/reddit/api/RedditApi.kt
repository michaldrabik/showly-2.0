package com.michaldrabik.data_remote.reddit.api

import com.michaldrabik.data_remote.Config
import com.michaldrabik.data_remote.reddit.model.RedditItem
import javax.inject.Inject

class RedditApi @Inject constructor(
  private val authApi: RedditAuthApi,
  private val listingApi: RedditListingApi,
) {

  suspend fun fetchAuthToken() = authApi.fetchAuthToken()

  suspend fun fetchTelevisionItems(
    token: String,
    limit: Int = Config.REDDIT_LIST_LIMIT,
    pages: Int = Config.REDDIT_LIST_PAGES,
  ): List<RedditItem> =
    listingApi.fetchTelevision(token, limit, pages).filterNot { it.is_self }

  suspend fun fetchMoviesItems(
    token: String,
    limit: Int = Config.REDDIT_LIST_LIMIT,
    pages: Int = Config.REDDIT_LIST_PAGES,
  ) = listingApi.fetchMovies(token, limit, pages).filterNot { it.is_self }
}
