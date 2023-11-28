package com.michaldrabik.data_remote.gcloud

import com.michaldrabik.data_remote.gcloud.model.NewsItem

/**
 * Fetch/post remote resources via GCloud API
 */
interface GCloudRemoteDataSource {

  suspend fun fetchTelevisionItems(): List<NewsItem>

  suspend fun fetchMoviesItems(): List<NewsItem>
}
