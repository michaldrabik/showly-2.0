package com.michaldrabik.ui_my_movies.mymovies.cases

import com.michaldrabik.common.Config
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.ui_base.images.MovieImagesProvider
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.MyMoviesSection
import com.michaldrabik.ui_model.MyMoviesSection.ALL
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortOrder.DATE_ADDED
import com.michaldrabik.ui_model.SortOrder.NAME
import com.michaldrabik.ui_model.SortOrder.NEWEST
import com.michaldrabik.ui_model.SortOrder.RATING
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_my_movies.mymovies.recycler.MyMoviesItem
import com.michaldrabik.ui_repository.SettingsRepository
import com.michaldrabik.ui_repository.TranslationsRepository
import com.michaldrabik.ui_repository.movies.MoviesRepository
import javax.inject.Inject

@AppScope
class MyMoviesLoadCase @Inject constructor(
  private val imagesProvider: MovieImagesProvider,
  private val moviesRepository: MoviesRepository,
  private val translationsRepository: TranslationsRepository,
  private val settingsRepository: SettingsRepository
) {

  val language by lazy { translationsRepository.getLanguage() }

  private suspend fun loadSettings() = settingsRepository.load()

  suspend fun loadAll() = moviesRepository.myMovies.loadAll()

  suspend fun filterSectionMovies(
    allMovies: List<MyMoviesItem>,
    section: MyMoviesSection
  ): List<MyMoviesItem> {
    val sortOrder = loadSortOrder(section)
    return sortBy(sortOrder, allMovies)
  }

  suspend fun loadRecentMovies(): List<Movie> {
    val amount = loadSettings().myRecentsAmount
    return moviesRepository.myMovies.loadAllRecent(amount)
  }

  suspend fun loadSortOrder(section: MyMoviesSection): SortOrder {
    val settings = loadSettings()
    return when (section) {
      ALL -> settings.myMoviesAllSortBy
      else -> error("Should not be used here.")
    }
  }

  private fun sortBy(sortOrder: SortOrder, movies: List<MyMoviesItem>) =
    when (sortOrder) {
      NAME -> movies.sortedBy {
        val translatedTitle = if (it.translation?.hasTitle == false) null else it.translation?.title
        translatedTitle ?: it.movie.title
      }
      NEWEST -> movies.sortedWith(compareByDescending<MyMoviesItem> { it.movie.year }.thenByDescending { it.movie.released })
      RATING -> movies.sortedByDescending { it.movie.rating }
      DATE_ADDED -> movies.sortedByDescending { it.movie.updatedAt }
      else -> error("Should not be used here.")
    }

  suspend fun setSectionSortOrder(section: MyMoviesSection, order: SortOrder) {
    val settings = loadSettings()
    val newSettings = when (section) {
      ALL -> settings.copy(myMoviesAllSortBy = order)
      else -> error("Should not be used here.")
    }
    settingsRepository.update(newSettings)
  }

  suspend fun loadTranslation(movie: Movie, onlyLocal: Boolean): Translation? {
    if (language == Config.DEFAULT_LANGUAGE) return Translation.EMPTY
    return translationsRepository.loadTranslation(movie, language, onlyLocal)
  }

  suspend fun findCachedImage(movie: Movie, type: ImageType) =
    imagesProvider.findCachedImage(movie, type)

  suspend fun loadMissingImage(movie: Movie, type: ImageType, force: Boolean) =
    imagesProvider.loadRemoteImage(movie, type, force)
}
