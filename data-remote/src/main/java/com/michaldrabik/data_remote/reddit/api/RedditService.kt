package com.michaldrabik.data_remote.reddit.api

import com.michaldrabik.data_remote.BuildConfig
import com.michaldrabik.data_remote.reddit.model.RedditAuthResponse
import com.michaldrabik.data_remote.reddit.model.RedditResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface RedditService {

  companion object {
    private const val USER_AGENT = "android:com.michaldrabik.showly2:v${BuildConfig.VER_NAME} (by /u/drabred)"
  }

  @Headers("User-Agent: $USER_AGENT")
  @POST("access_token")
  suspend fun fetchAccessToken(
    @Header("Authorization") credentials: String,
    @Query("grant_type") grantType: String,
    @Query("device_id") deviceId: String,
  ): RedditAuthResponse

  @Headers("User-Agent: $USER_AGENT")
  @GET("r/television/hot/.json")
  suspend fun fetchTelevision(
    @Header("Authorization") token: String,
    @Query("limit") limit: Int,
    @Query("after") after: String? = null,
  ): RedditResponse

  @Headers("User-Agent: $USER_AGENT")
  @GET("r/movies/hot/.json")
  suspend fun fetchMovies(
    @Header("Authorization") token: String,
    @Query("limit") limit: Int,
    @Query("after") after: String? = null,
  ): RedditResponse
}
