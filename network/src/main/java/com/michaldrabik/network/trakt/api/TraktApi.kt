package com.michaldrabik.network.trakt.api

import com.michaldrabik.network.trakt.model.Episode
import com.michaldrabik.network.trakt.model.Show

class TraktApi(private val service: TraktService) {

  suspend fun fetchShow(traktId: Long) = service.fetchShow(traktId)

  suspend fun fetchTrendingShows() = service.fetchTrendingShows().map { it.show }

  suspend fun fetchNextEpisode(traktId: Long): Episode? {
    val response = service.fetchNextEpisode(traktId)
    if (response.isSuccessful && response.code() == 204) return null
    return response.body()
  }

  suspend fun searchForShow(query: String): List<Show> {
    val results = service.fetchSearchResults(query)
    return results.sortedWith(compareBy({ it.score }, { it.show.votes })).reversed().map { it.show }
  }
}