package com.michaldrabik.ui_my_shows.watchlist.cases

import com.michaldrabik.common.Config
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.images.ShowImagesProvider
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_model.TraktRating
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_my_shows.watchlist.helpers.WatchlistItemSorter
import com.michaldrabik.ui_my_shows.watchlist.recycler.WatchlistListItem
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@ViewModelScoped
class WatchlistLoadShowsCase @Inject constructor(
  private val ratingsCase: WatchlistRatingsCase,
  private val sorter: WatchlistItemSorter,
  private val showsRepository: ShowsRepository,
  private val translationsRepository: TranslationsRepository,
  private val settingsRepository: SettingsRepository,
  private val imagesProvider: ShowImagesProvider,
  private val dateFormatProvider: DateFormatProvider,
) {

  val language by lazy { translationsRepository.getLanguage() }

  suspend fun loadShows(searchQuery: String): List<WatchlistListItem> = coroutineScope {
    val ratings = ratingsCase.loadRatings()
    val dateFormat = dateFormatProvider.loadFullDayFormat()
    val translations =
      if (language == Config.DEFAULT_LANGUAGE) emptyMap()
      else translationsRepository.loadAllShowsLocal(language)

    val sortOrder = settingsRepository.sorting.watchlistShowsSortOrder
    val sortType = settingsRepository.sorting.watchlistShowsSortType

    val showsItems = showsRepository.watchlistShows.loadAll()
      .map {
        toListItemAsync(
          show = it,
          translation = translations[it.traktId],
          userRating = ratings[it.ids.trakt],
          dateFormat = dateFormat
        )
      }
      .awaitAll()
      .filterByQuery(searchQuery)
      .sortedWith(sorter.sort(sortOrder, sortType))

    if (showsItems.isNotEmpty()) {
      val filtersItem = loadFiltersItem(sortOrder, sortType)
      listOf(filtersItem) + showsItems
    } else {
      showsItems
    }
  }

  private fun List<WatchlistListItem.ShowItem>.filterByQuery(query: String) =
    this.filter {
      it.show.title.contains(query, true) ||
        it.translation?.title?.contains(query, true) == true
    }

  private fun loadFiltersItem(
    sortOrder: SortOrder,
    sortType: SortType,
  ): WatchlistListItem.FiltersItem {
    return WatchlistListItem.FiltersItem(
      sortOrder = sortOrder,
      sortType = sortType
    )
  }

  private fun CoroutineScope.toListItemAsync(
    show: Show,
    translation: Translation?,
    userRating: TraktRating?,
    dateFormat: DateTimeFormatter,
  ) = async {
    val image = imagesProvider.findCachedImage(show, ImageType.POSTER)
    WatchlistListItem.ShowItem(
      isLoading = false,
      show = show,
      image = image,
      dateFormat = dateFormat,
      translation = translation,
      userRating = userRating?.rating
    )
  }
}
