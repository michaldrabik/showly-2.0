package com.michaldrabik.data_remote.reddit.api

import com.michaldrabik.data_remote.reddit.model.RedditItem

internal class RedditListingApi(private val service: RedditService) {

  suspend fun fetchTelevision(token: String, limit: Int, pages: Int): List<RedditItem> {
    val result = mutableListOf<RedditItem>()
    var after: String? = null
    (0 until pages).forEach { _ ->
      val response = service.fetchTelevision("Bearer $token", limit, after)
      result.addAll(response.data.children.map { it.data })
      after = response.data.after
    }
    return result
  }

  suspend fun fetchMovies(token: String, limit: Int, pages: Int): List<RedditItem> {
    val result = mutableListOf<RedditItem>()
    var after: String? = null
    (0 until pages).forEach { _ ->
      val response = service.fetchMovies("Bearer $token", limit, after)
      result.addAll(response.data.children.map { it.data })
      after = response.data.after
    }
    return result
  }
}
