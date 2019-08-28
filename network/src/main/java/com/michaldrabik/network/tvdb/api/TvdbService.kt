package com.michaldrabik.network.tvdb.api

import com.michaldrabik.network.tvdb.model.AuthorizationRequest
import com.michaldrabik.network.tvdb.model.AuthorizationToken
import com.michaldrabik.network.tvdb.model.TvdbImagesResult
import retrofit2.http.*

interface TvdbService {

  @POST("/login")
  suspend fun authorize(@Body body: AuthorizationRequest): AuthorizationToken

  @GET("/refresh_token")
  suspend fun refreshToken(@Header("Authorization") authorization: String): AuthorizationToken

  @GET("/series/{id}/images/query")
  suspend fun fetchImages(
    @Header("Authorization") authorization: String,
    @Path("id") id: Long,
    @Query("keyType") type: String
  ): TvdbImagesResult
}