package com.michaldrabik.network.aws.api

import com.michaldrabik.network.aws.model.AwsImages

class AwsApi(private val service: AwsService) {

  suspend fun fetchImagesList() =
    try {
      service.fetchImagesList().shows
    } catch (error: Throwable) {
      AwsImages(emptyList(), emptyList())
    }
}
