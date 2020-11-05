package com.michaldrabik.network.aws.api

class AwsApi(private val service: AwsService) {

  suspend fun fetchAvailablePosters() =
    try {
      service.fetchImagesList().shows.posters
    } catch (error: Throwable) {
      emptyList()
    }
}
