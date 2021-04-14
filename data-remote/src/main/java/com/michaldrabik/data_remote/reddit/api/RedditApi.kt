package com.michaldrabik.data_remote.reddit.api

import com.michaldrabik.data_remote.Config.REDDIT_DEFAULT_LIMIT
import com.michaldrabik.data_remote.reddit.model.RedditItem

class RedditApi(private val service: RedditService) {

  suspend fun fetchTelevision(limit: Int = REDDIT_DEFAULT_LIMIT): List<RedditItem> {
    val response = service.fetchTelevision(limit)
    return response.data.children.map { it.data }
  }

  suspend fun fetchMovies(limit: Int = REDDIT_DEFAULT_LIMIT): List<RedditItem> {
    val response = service.fetchMovies(limit)
    return response.data.children.map { it.data }
  }
}
