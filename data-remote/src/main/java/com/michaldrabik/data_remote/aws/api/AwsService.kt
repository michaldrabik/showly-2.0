package com.michaldrabik.data_remote.aws.api

import com.michaldrabik.data_remote.aws.model.AwsImagesList
import retrofit2.http.GET

interface AwsService {

  @GET("images/images.json")
  suspend fun fetchImagesList(): AwsImagesList
}
