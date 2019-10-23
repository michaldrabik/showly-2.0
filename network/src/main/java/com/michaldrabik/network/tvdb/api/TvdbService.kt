package com.michaldrabik.network.tvdb.api

import com.michaldrabik.network.tvdb.model.AuthorizationRequest
import com.michaldrabik.network.tvdb.model.AuthorizationToken
import com.michaldrabik.network.tvdb.model.TvdbActor
import com.michaldrabik.network.tvdb.model.TvdbEpisodeImageResult
import com.michaldrabik.network.tvdb.model.TvdbImage
import com.michaldrabik.network.tvdb.model.TvdbResult
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

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
    @Path("id") id: Long
  ): TvdbEpisodeImageResult
}