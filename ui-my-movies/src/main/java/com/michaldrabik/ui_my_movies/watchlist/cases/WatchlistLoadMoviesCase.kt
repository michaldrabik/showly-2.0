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
import com.michaldrabik.ui_my_movies.watchlist.helpers.WatchlistItemFilter
import com.michaldrabik.ui_my_movies.watchlist.helpers.WatchlistItemSorter
import com.michaldrabik.ui_my_movies.watchlist.recycler.WatchlistListItem
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
  private val sorter: WatchlistItemSorter,
  private val filters: WatchlistItemFilter,
  private val moviesRepository: MoviesRepository,
  private val translationsRepository: TranslationsRepository,
  private val dateFormatProvider: DateFormatProvider,
  private val imagesProvider: MovieImagesProvider,
  private val settingsRepository: SettingsRepository,
) {

  val language by lazy { translationsRepository.getLanguage() }

  suspend fun loadMovies(searchQuery: String): List<WatchlistListItem> = coroutineScope {
    val ratings = ratingsCase.loadRatings()
    val dateFormat = dateFormatProvider.loadShortDayFormat()
    val fullDateFormat = dateFormatProvider.loadFullDayFormat()
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
      .awaitAll().filter {
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

  private fun loadFiltersItem(): WatchlistListItem.FiltersItem {
    return WatchlistListItem.FiltersItem(
      sortOrder = settingsRepository.sorting.watchlistMoviesSortOrder,
      sortType = settingsRepository.sorting.watchlistMoviesSortType,
      isUpcoming = settingsRepository.filters.watchlistMoviesUpcoming
    )
  }

  suspend fun loadTranslation(movie: Movie, onlyLocal: Boolean): Translation? {
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
    WatchlistListItem.MovieItem(
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
