package com.michaldrabik.data_remote.reddit.api

import com.michaldrabik.data_remote.reddit.model.RedditItem

class RedditListingApi(private val service: RedditService) {

  suspend fun fetchTelevision(token: String, limit: Int): List<RedditItem> {
    val response = service.fetchTelevision("Bearer $token", limit)
    return response.data.children.map { it.data }
  }

  suspend fun fetchMovies(token: String, limit: Int): List<RedditItem> {
    val response = service.fetchMovies("Bearer $token", limit)
    return response.data.children.map { it.data }
  }
}
