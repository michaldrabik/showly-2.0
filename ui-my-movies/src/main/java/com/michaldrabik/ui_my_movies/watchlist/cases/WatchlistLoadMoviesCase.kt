package com.michaldrabik.ui_my_movies.watchlist.cases

import com.michaldrabik.common.Config
import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.SortOrder.DATE_ADDED
import com.michaldrabik.ui_model.SortOrder.NAME
import com.michaldrabik.ui_model.SortOrder.NEWEST
import com.michaldrabik.ui_model.SortOrder.RATING
import com.michaldrabik.ui_model.Translation
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class WatchlistLoadMoviesCase @Inject constructor(
  private val moviesRepository: MoviesRepository,
  private val translationsRepository: TranslationsRepository,
  private val dateFormatProvider: DateFormatProvider,
  private val settingsRepository: SettingsRepository
) {

  val language by lazy { translationsRepository.getLanguage() }

  suspend fun loadMovies(): List<Pair<Movie, Translation?>> {
    val translations =
      if (language == Config.DEFAULT_LANGUAGE) emptyMap()
      else translationsRepository.loadAllMoviesLocal(language)

    val sortType = settingsRepository.load().watchlistMoviesSortBy
    val movies = moviesRepository.watchlistMovies.loadAll()
      .map { it to translations[it.traktId] }

    return when (sortType) {
      NAME -> movies.sortedBy {
        val translatedTitle = if (it.second?.hasTitle == false) null else it.second?.title
        translatedTitle ?: it.first.titleNoThe
      }
      DATE_ADDED ->
        movies.sortedByDescending { it.first.updatedAt }
      RATING ->
        movies.sortedByDescending { it.first.rating }
      NEWEST ->
        movies.sortedWith(
          compareByDescending<Pair<Movie, Translation?>> { it.first.released }
            .thenByDescending { it.first.year }
        )
      else -> error("Should not be used here.")
    }
  }

  suspend fun loadTranslation(movie: Movie, onlyLocal: Boolean): Translation? {
    if (language == Config.DEFAULT_LANGUAGE) return Translation.EMPTY
    return translationsRepository.loadTranslation(movie, language, onlyLocal)
  }

  fun loadDateFormat() = dateFormatProvider.loadShortDayFormat()
}
