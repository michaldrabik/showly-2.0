package com.michaldrabik.network.tvdb.api

import com.michaldrabik.network.Config
import com.michaldrabik.network.tvdb.model.AuthorizationRequest

class TvdbApi(private val service: TvdbService) {

  suspend fun authorize() = service.authorize(
    AuthorizationRequest(
      apikey = Config.TVDB_API_KEY,
      username = Config.TVDB_USER,
      userkey = Config.TVDB_CLIENT_ID
    )
  )

  suspend fun refreshToken(token: String) =
    service.refreshToken("Bearer $token")

  suspend fun fetchPosterImages(token: String, tvdbId: Long) =
    service.fetchImages("Bearer $token", tvdbId, "poster").data

  suspend fun fetchFanartImages(token: String, tvdbId: Long) =
    service.fetchImages("Bearer $token", tvdbId, "fanart").data
}