package com.michaldrabik.data_remote.gcloud.api

import com.michaldrabik.data_remote.gcloud.model.NewsItem
import retrofit2.http.GET

interface GCloudService {

  @GET("news/television")
  suspend fun fetchTelevision(): List<NewsItem>

  @GET("news/movies")
  suspend fun fetchMovies(): List<NewsItem>
}
