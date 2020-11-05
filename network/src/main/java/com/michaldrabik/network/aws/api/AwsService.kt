package com.michaldrabik.network.aws.api

import com.michaldrabik.network.aws.model.AwsImagesList
import retrofit2.http.GET

interface AwsService {

  @GET("images/images.json")
  suspend fun fetchImagesList(): AwsImagesList
}
