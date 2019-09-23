package com.michaldrabik.network.tvdb.api

import com.michaldrabik.network.tvdb.model.*
import retrofit2.http.*

interface TvdbService {

  @POST("/login")
  suspend fun authorize(@Body body: AuthorizationRequest): AuthorizationToken

  @GET("/refresh_token")
  suspend fun refreshToken(@Header("Authorization") authorization: String): AuthorizationToken

  @GET("/series/{id}/actors")
  suspend fun fetchActors(
    @Header("Authorization") authorization: String,
    @Path("id") id: Long
  ): TvdbResult<TvdbActor>

  @GET("/series/{id}/images/query")
  suspend fun fetchShowImages(
    @Header("Authorization") authorization: String,
    @Path("id") id: Long,
    @Query("keyType") type: String
  ): TvdbResult<TvdbImage>

  @GET("/episodes/{id}")
  suspend fun fetchEpisodeImage(
    @Header("Authorization") authorization: String,
    @Path("id") id: Long): TvdbEpisodeImageResult
}