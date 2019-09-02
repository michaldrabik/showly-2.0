package com.michaldrabik.network.trakt.api

import com.michaldrabik.network.trakt.model.Episode

class TraktApi(private val service: TraktService) {

  suspend fun fetchTrendingShows() = service.fetchTrendingShows().map { it.show }

  suspend fun fetchNextEpisode(traktId: Long): Episode? {
    val response = service.fetchNextEpisode(traktId)
    if (response.isSuccessful && response.code() == 204) return null
    return response.body()
  }
}