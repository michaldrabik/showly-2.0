package com.michaldrabik.data_remote.aws.api

import com.michaldrabik.data_remote.aws.model.AwsImages

class AwsApi(private val service: AwsService) {

  suspend fun fetchImagesList() =
    try {
      service.fetchImagesList().shows
    } catch (error: Throwable) {
      AwsImages(emptyList(), emptyList())
    }
}
