package com.michaldrabik.network.tvdb.api

import com.michaldrabik.network.Config
import com.michaldrabik.network.tvdb.model.AuthorizationRequest
import com.michaldrabik.network.tvdb.model.TvdbActor
import com.michaldrabik.network.tvdb.model.TvdbImage
import com.michaldrabik.network.tvdb.model.TvdbImageRating

class TvdbApi(private val service: TvdbService) {

  suspend fun authorize() = service.authorize(
    AuthorizationRequest(apiKey = Config.TVDB_API_KEY)
  )

  suspend fun fetchShowImages(token: String, tvdbId: Long): List<TvdbImage> {
    return try {
      service.fetchShowImages("Bearer $token", tvdbId).data
        .map {
          it.copy(
            fileName = removeExtraPrefix(it.fileName),
            thumbnail = removeExtraPrefix(it.thumbnail)
          )
        }
    } catch (t: Throwable) {
      emptyList()
    }
  }

  suspend fun fetchEpisodeImage(token: String, tvdbId: Long): TvdbImage? {
    val result = service.fetchEpisodeImage("Bearer $token", tvdbId).data
    if (result.filename.isNullOrBlank()) return null
    return TvdbImage(result.id ?: -1, removeExtraPrefix(result.filename), "", "", TvdbImageRating(0F, 0))
  }

  suspend fun fetchActors(token: String, tvdbId: Long): List<TvdbActor> {
    return service.fetchActors("Bearer $token", tvdbId).data
      .map {
        it.copy(image = removeExtraPrefix(it.image))
      }
  }

  private fun removeExtraPrefix(input: String) = input.removePrefix("/")
}