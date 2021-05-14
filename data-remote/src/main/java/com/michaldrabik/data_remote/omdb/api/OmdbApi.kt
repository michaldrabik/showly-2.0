package com.michaldrabik.data_remote.omdb.api

class OmdbApi(private val service: OmdbService) {

  suspend fun fetchOmdbData(imdbId: String) =
    service.fetchData(imdbId)
}
