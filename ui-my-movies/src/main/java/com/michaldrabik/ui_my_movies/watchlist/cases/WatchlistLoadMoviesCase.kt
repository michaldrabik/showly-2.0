package com.michaldrabik.ui_my_movies.watchlist.cases

import com.michaldrabik.common.Config
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.images.MovieImagesProvider
import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.TraktRating
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_my_movies.common.helpers.CollectionItemFilter
import com.michaldrabik.ui_my_movies.common.helpers.CollectionItemSorter
import com.michaldrabik.ui_my_movies.common.recycler.CollectionListItem
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@ViewModelScoped
class WatchlistLoadMoviesCase @Inject constructor(
  private val ratingsCase: WatchlistRatingsCase,
  private val sorter: CollectionItemSorter,
  private val filters: CollectionItemFilter,
  private val moviesRepository: MoviesRepository,
  private val translationsRepository: TranslationsRepository,
  private val dateFormatProvider: DateFormatProvider,
  private val imagesProvider: MovieImagesProvider,
  private val settingsRepository: SettingsRepository,
) {

  suspend fun loadMovies(searchQuery: String): List<CollectionListItem> = coroutineScope {
    val ratings = ratingsCase.loadRatings()
    val dateFormat = dateFormatProvider.loadShortDayFormat()
    val fullDateFormat = dateFormatProvider.loadFullDayFormat()
    val language = translationsRepository.getLanguage()
    val translations =
      if (language == Config.DEFAULT_LANGUAGE) emptyMap()
      else translationsRepository.loadAllMoviesLocal(language)

    val filtersItem = loadFiltersItem()
    val moviesItems = moviesRepository.watchlistMovies.loadAll()
      .map {
        toListItemAsync(
          movie = it,
          translation = translations[it.traktId],
          userRating = ratings[it.ids.trakt],
          dateFormat = dateFormat,
          fullDateFormat = fullDateFormat
        )
      }
      .awaitAll()
      .filter {
        filters.filterByQuery(it, searchQuery) &&
          filters.filterUpcoming(it, filtersItem.isUpcoming)
      }
      .sortedWith(sorter.sort(filtersItem.sortOrder, filtersItem.sortType))

    if (moviesItems.isNotEmpty() || filtersItem.isUpcoming) {
      listOf(filtersItem) + moviesItems
    } else {
      moviesItems
    }
  }

  private fun loadFiltersItem(): CollectionListItem.FiltersItem {
    return CollectionListItem.FiltersItem(
      sortOrder = settingsRepository.sorting.watchlistMoviesSortOrder,
      sortType = settingsRepository.sorting.watchlistMoviesSortType,
      isUpcoming = settingsRepository.filters.watchlistMoviesUpcoming
    )
  }

  suspend fun loadTranslation(movie: Movie, onlyLocal: Boolean): Translation? {
    val language = translationsRepository.getLanguage()
    if (language == Config.DEFAULT_LANGUAGE) return Translation.EMPTY
    return translationsRepository.loadTranslation(movie, language, onlyLocal)
  }

  private fun CoroutineScope.toListItemAsync(
    movie: Movie,
    translation: Translation?,
    userRating: TraktRating?,
    dateFormat: DateTimeFormatter,
    fullDateFormat: DateTimeFormatter,
  ) = async {
    val image = imagesProvider.findCachedImage(movie, ImageType.POSTER)
    CollectionListItem.MovieItem(
      isLoading = false,
      movie = movie,
      image = image,
      dateFormat = dateFormat,
      fullDateFormat = fullDateFormat,
      translation = translation,
      userRating = userRating?.rating
    )
  }
}
