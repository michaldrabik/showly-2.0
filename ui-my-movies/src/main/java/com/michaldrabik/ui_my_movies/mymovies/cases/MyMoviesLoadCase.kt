package com.michaldrabik.ui_my_movies.mymovies.cases

import com.michaldrabik.common.Config
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.images.MovieImagesProvider
import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_my_movies.mymovies.helpers.MyMoviesSorter
import com.michaldrabik.ui_my_movies.mymovies.recycler.MyMoviesItem
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class MyMoviesLoadCase @Inject constructor(
  private val sorter: MyMoviesSorter,
  private val imagesProvider: MovieImagesProvider,
  private val moviesRepository: MoviesRepository,
  private val dateFormatProvider: DateFormatProvider,
  private val translationsRepository: TranslationsRepository,
  private val settingsRepository: SettingsRepository,
) {

  suspend fun loadSettings() = settingsRepository.load()

  suspend fun loadAll() = moviesRepository.myMovies.loadAll()

  fun filterSectionMovies(
    allMovies: List<MyMoviesItem>,
    sortOrder: Pair<SortOrder, SortType>,
    searchQuery: String? = null,
  ) = allMovies
    .filterByQuery(searchQuery)
    .sortedWith(sorter.sort(sortOrder.first, sortOrder.second))

  private fun List<MyMoviesItem>.filterByQuery(query: String?) = when {
    query.isNullOrBlank() -> this
    else -> this.filter {
      it.movie.title.contains(query, true) ||
        it.translation?.title?.contains(query, true) == true
    }
  }

  suspend fun loadRecentMovies(): List<Movie> {
    val amount = loadSettings().myRecentsAmount
    return moviesRepository.myMovies.loadAllRecent(amount)
  }

  suspend fun loadTranslation(movie: Movie, onlyLocal: Boolean): Translation? {
    val language = translationsRepository.getLanguage()
    if (language == Config.DEFAULT_LANGUAGE) return Translation.EMPTY
    return translationsRepository.loadTranslation(movie, language, onlyLocal)
  }

  fun loadDateFormat() = dateFormatProvider.loadShortDayFormat()

  suspend fun findCachedImage(movie: Movie, type: ImageType) =
    imagesProvider.findCachedImage(movie, type)

  suspend fun loadMissingImage(movie: Movie, type: ImageType, force: Boolean) =
    imagesProvider.loadRemoteImage(movie, type, force)
}
