package com.michaldrabik.data_remote.reddit

import com.michaldrabik.data_remote.Config
import com.michaldrabik.data_remote.reddit.model.RedditAuthResponse
import com.michaldrabik.data_remote.reddit.model.RedditItem

/**
 * Fetch/post remote resources via Reddit API
 */
interface RedditRemoteDataSource {

  suspend fun fetchAuthToken(): RedditAuthResponse

  suspend fun fetchTelevisionItems(
    token: String,
    limit: Int = Config.REDDIT_LIST_LIMIT,
    pages: Int = Config.REDDIT_LIST_PAGES,
  ): List<RedditItem>

  suspend fun fetchMoviesItems(
    token: String,
    limit: Int = Config.REDDIT_LIST_LIMIT,
    pages: Int = Config.REDDIT_LIST_PAGES,
  ): List<RedditItem>
}
