package com.michaldrabik.ui_my_movies.watchlist.cases

import com.michaldrabik.common.Config
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.SortOrder.DATE_ADDED
import com.michaldrabik.ui_model.SortOrder.NAME
import com.michaldrabik.ui_model.SortOrder.NEWEST
import com.michaldrabik.ui_model.SortOrder.RATING
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_repository.SettingsRepository
import com.michaldrabik.ui_repository.TranslationsRepository
import com.michaldrabik.ui_repository.movies.MoviesRepository
import javax.inject.Inject

@AppScope
class WatchlistLoadMoviesCase @Inject constructor(
  private val moviesRepository: MoviesRepository,
  private val translationsRepository: TranslationsRepository,
  private val settingsRepository: SettingsRepository
) {

  suspend fun loadMovies(): List<Movie> {
    val sortType = settingsRepository.load().watchlistMoviesSortBy
    val movies = moviesRepository.watchlistMovies.loadAll()
    return when (sortType) {
      NAME -> movies.sortedBy { it.title }
      DATE_ADDED -> movies.sortedByDescending { it.updatedAt }
      RATING -> movies.sortedByDescending { it.rating }
      NEWEST -> movies.sortedWith(compareByDescending<Movie> { it.year }.thenByDescending { it.released })
      else -> error("Should not be used here.")
    }
  }

  suspend fun loadTranslation(movie: Movie): Translation? {
    val language = settingsRepository.getLanguage()
    if (language == Config.DEFAULT_LANGUAGE) return null
    return translationsRepository.loadTranslation(movie, language, onlyLocal = true)
  }
}
