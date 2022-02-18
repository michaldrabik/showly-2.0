package com.michaldrabik.ui_my_movies.watchlist.cases

import com.michaldrabik.common.Config
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_my_movies.utilities.FollowedMoviesItemSorter
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class WatchlistLoadMoviesCase @Inject constructor(
  private val sorter: FollowedMoviesItemSorter,
  private val moviesRepository: MoviesRepository,
  private val translationsRepository: TranslationsRepository,
  private val dateFormatProvider: DateFormatProvider,
  private val settingsRepository: SettingsRepository
) {

  val language by lazy { translationsRepository.getLanguage() }

  suspend fun loadMovies(searchQuery: String): List<Pair<Movie, Translation?>> {
    val translations =
      if (language == Config.DEFAULT_LANGUAGE) emptyMap()
      else translationsRepository.loadAllMoviesLocal(language)

    val sortOrder = settingsRepository.sorting.watchlistMoviesSortOrder
    val sortType = settingsRepository.sorting.watchlistMoviesSortType

    return moviesRepository.watchlistMovies.loadAll()
      .map { it to translations[it.traktId] }
      .filterByQuery(searchQuery)
      .sortedWith(sorter.sort(sortOrder, sortType))
  }

  private fun List<Pair<Movie, Translation?>>.filterByQuery(query: String) =
    this.filter {
      it.first.title.contains(query, true) ||
        it.second?.title?.contains(query, true) == true
    }

  suspend fun loadTranslation(movie: Movie, onlyLocal: Boolean): Translation? {
    if (language == Config.DEFAULT_LANGUAGE) return Translation.EMPTY
    return translationsRepository.loadTranslation(movie, language, onlyLocal)
  }

  fun loadDateFormat() = dateFormatProvider.loadShortDayFormat()
}
