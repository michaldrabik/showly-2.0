package com.michaldrabik.network.tvdb.api

import com.michaldrabik.network.Config
import com.michaldrabik.network.tvdb.model.AuthorizationRequest
import com.michaldrabik.network.tvdb.model.TvdbActor
import com.michaldrabik.network.tvdb.model.TvdbImage

class TvdbApi(private val service: TvdbService) {

  private val allowedTypes = arrayOf("poster", "fanart")

  suspend fun authorize() = service.authorize(
    AuthorizationRequest(
      apikey = Config.TVDB_API_KEY,
      username = Config.TVDB_USER,
      userkey = Config.TVDB_CLIENT_ID
    )
  )

  suspend fun refreshToken(token: String) =
    service.refreshToken("Bearer $token")

  suspend fun fetchImages(token: String, tvdbId: Long, type: String): List<TvdbImage> {
    check(type in allowedTypes)
    return service.fetchImages("Bearer $token", tvdbId, type).data
  }

  suspend fun fetchActors(token: String, tvdbId: Long): List<TvdbActor> {
    return service.fetchActors("Bearer $token", tvdbId).data
  }
}