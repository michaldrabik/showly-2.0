package com.michaldrabik.network.trakt.api

import com.michaldrabik.network.trakt.model.Episode
import com.michaldrabik.network.trakt.model.Show

class TraktApi(private val service: TraktService) {

  suspend fun fetchShow(traktId: Long) = service.fetchShow(traktId)

  suspend fun fetchTrendingShows() = service.fetchTrendingShows().map { it.show }

  suspend fun fetchRelatedShows(traktId: Long) = service.fetchRelatedShows(traktId)

  suspend fun fetchNextEpisode(traktId: Long): Episode? {
    val response = service.fetchNextEpisode(traktId)
    if (response.isSuccessful && response.code() == 204) return null
    return response.body()
  }

  suspend fun fetchShowsSearch(query: String): List<Show> {
    val results = service.fetchSearchResults(query)
    return results.sortedWith(compareBy({ it.show.votes }, { it.score })).reversed().map { it.show }
  }
}