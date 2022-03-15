package com.michaldrabik.data_remote.reddit.api

import com.michaldrabik.data_remote.reddit.RedditRemoteDataSource
import com.michaldrabik.data_remote.reddit.model.RedditItem

internal class RedditApi(
  private val authApi: RedditAuthApi,
  private val listingApi: RedditListingApi,
) : RedditRemoteDataSource {

  override suspend fun fetchAuthToken() = authApi.fetchAuthToken()

  override suspend fun fetchTelevisionItems(
    token: String,
    limit: Int,
    pages: Int,
  ): List<RedditItem> =
    listingApi.fetchTelevision(token, limit, pages).filterNot { it.is_self }

  override suspend fun fetchMoviesItems(
    token: String,
    limit: Int,
    pages: Int,
  ) = listingApi.fetchMovies(token, limit, pages).filterNot { it.is_self }
}
