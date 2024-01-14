package com.michaldrabik.data_remote.aws.api

import com.michaldrabik.data_remote.aws.AwsRemoteDataSource
import com.michaldrabik.data_remote.aws.model.AwsImages

internal class AwsApi(private val service: AwsService) : AwsRemoteDataSource {

  override suspend fun fetchImagesList() =
    try {
      service.fetchImagesList().shows
    } catch (error: Throwable) {
      AwsImages(emptyList(), emptyList())
    }
}
