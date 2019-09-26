package com.michaldrabik.network.tvdb.api

import com.michaldrabik.network.Config
import com.michaldrabik.network.tvdb.model.AuthorizationRequest
import com.michaldrabik.network.tvdb.model.TvdbActor
import com.michaldrabik.network.tvdb.model.TvdbImage
import com.michaldrabik.network.tvdb.model.TvdbImageRating

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

  suspend fun fetchShowImages(token: String, tvdbId: Long, type: String): List<TvdbImage> {
    check(type in allowedTypes)
    return try {
      service.fetchShowImages("Bearer $token", tvdbId, type).data
    } catch (e: Exception) {
      emptyList()
    }
  }

  suspend fun fetchEpisodeImage(token: String, tvdbId: Long): TvdbImage? {
    val result = service.fetchEpisodeImage("Bearer $token", tvdbId).data
    if (result.filename.isNullOrBlank()) return null
    return TvdbImage(result.id ?: -1, result.filename, "", TvdbImageRating(0F, 0))
  }

  suspend fun fetchActors(token: String, tvdbId: Long): List<TvdbActor> {
    return service.fetchActors("Bearer $token", tvdbId).data
  }
}