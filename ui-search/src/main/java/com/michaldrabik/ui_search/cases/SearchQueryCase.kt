package com.michaldrabik.ui_search.cases

import com.michaldrabik.data_remote.Cloud
import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.SearchResult
import com.michaldrabik.ui_model.Show
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject
import com.michaldrabik.data_remote.trakt.model.SearchResult as SearchResultNetwork

@ViewModelScoped
class SearchQueryCase @Inject constructor(
  private val cloud: Cloud,
  private val mappers: Mappers,
  private val settingsRepository: SettingsRepository,
) {

  suspend fun searchByQuery(query: String): List<SearchResult> {
    val withMovies = settingsRepository.isMoviesEnabled

    val results = cloud.traktApi.fetchSearch(query, withMovies)

    return results
      .sortedWith(compareByDescending<SearchResultNetwork> { it.score }.thenByDescending { it.getVotes() })
      .map {
        SearchResult(
          it.score ?: 0F,
          it.show?.let { s -> mappers.show.fromNetwork(s) } ?: Show.EMPTY,
          it.movie?.let { m -> mappers.movie.fromNetwork(m) } ?: Movie.EMPTY
        )
      }
  }
}
