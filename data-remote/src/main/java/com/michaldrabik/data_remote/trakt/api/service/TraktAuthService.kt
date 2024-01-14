package com.michaldrabik.data_remote.trakt.api.service

import com.michaldrabik.data_remote.trakt.model.OAuthResponse
import com.michaldrabik.data_remote.trakt.model.request.OAuthRefreshRequest
import com.michaldrabik.data_remote.trakt.model.request.OAuthRequest
import com.michaldrabik.data_remote.trakt.model.request.OAuthRevokeRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface TraktAuthService {

  @POST("oauth/token")
  suspend fun fetchOAuthToken(@Body request: OAuthRequest): OAuthResponse

  @POST("oauth/token")
  suspend fun refreshOAuthToken(@Body request: OAuthRefreshRequest): OAuthResponse

  @POST("oauth/revoke")
  suspend fun revokeOAuthToken(@Body request: OAuthRevokeRequest): Response<Any>
}
