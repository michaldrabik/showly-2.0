package com.michaldrabik.data_remote.gcloud.api

import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.michaldrabik.data_remote.gcloud.GCloudRemoteDataSource
import com.michaldrabik.data_remote.gcloud.model.NewsItem

internal class GCloudApi(
  private val service: GCloudService
) : GCloudRemoteDataSource {

  override suspend fun fetchTelevisionItems(): List<NewsItem> {
    Firebase.analytics.logEvent("news_fetch_television_gcloud", null)
    return service.fetchTelevision()
      .filterNot { it.is_self }
  }

  override suspend fun fetchMoviesItems(): List<NewsItem> {
    Firebase.analytics.logEvent("news_fetch_movies_gcloud", null)
    return service.fetchMovies()
      .filterNot { it.is_self }
  }
}
