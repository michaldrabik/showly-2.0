package com.michaldrabik.ui_my_movies.hidden.cases

import com.michaldrabik.common.Config
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_base.images.MovieImagesProvider
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_my_movies.hidden.recycler.HiddenListItem
import com.michaldrabik.ui_my_movies.utilities.FollowedMoviesItemSorter
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

@ViewModelScoped
class HiddenLoadMoviesCase @Inject constructor(
  private val sorter: FollowedMoviesItemSorter,
  private val moviesRepository: MoviesRepository,
  private val translationsRepository: TranslationsRepository,
  private val imagesProvider: MovieImagesProvider,
  private val dateFormatProvider: DateFormatProvider,
  private val settingsRepository: SettingsRepository
) {

  val language by lazy { translationsRepository.getLanguage() }

  suspend fun loadMovies(searchQuery: String): List<HiddenListItem> = coroutineScope {
    val dateFormat = dateFormatProvider.loadShortDayFormat()
    val translations =
      if (language == Config.DEFAULT_LANGUAGE) emptyMap()
      else translationsRepository.loadAllMoviesLocal(language)

    val sortOrder = settingsRepository.sortSettings.hiddenMoviesSortOrder
    val sortType = settingsRepository.sortSettings.hiddenMoviesSortType

    moviesRepository.hiddenMovies.loadAll()
      .map { it to translations[it.traktId] }
      .filterByQuery(searchQuery)
      .sortedWith(sorter.sort(sortOrder, sortType))
      .map {
        async {
          val image = imagesProvider.findCachedImage(it.first, ImageType.POSTER)
          HiddenListItem(
            movie = it.first,
            image = image,
            translation = it.second,
            isLoading = false,
            dateFormat = dateFormat
          )
        }
      }.awaitAll()
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
}
