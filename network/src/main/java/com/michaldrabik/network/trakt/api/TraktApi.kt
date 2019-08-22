package com.michaldrabik.network.trakt.api

class TraktApi(private val service: TraktService) {

  suspend fun fetchTrendingShows() = service.fetchTrendingShows().map { it.show }

}