package com.michaldrabik.ui_my_shows.hidden.cases

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
import com.michaldrabik.ui_my_shows.common.recycler.CollectionListItem
import com.michaldrabik.ui_my_shows.hidden.helpers.HiddenItemSorter
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@ViewModelScoped
class HiddenLoadShowsCase @Inject constructor(
  private val ratingsCase: HiddenRatingsCase,
  private val sorter: HiddenItemSorter,
  private val showsRepository: ShowsRepository,
  private val translationsRepository: TranslationsRepository,
  private val settingsRepository: SettingsRepository,
  private val imagesProvider: ShowImagesProvider,
  private val dateFormatProvider: DateFormatProvider,
) {

  suspend fun loadShows(searchQuery: String): List<CollectionListItem> = coroutineScope {
    val language = translationsRepository.getLanguage()
    val ratings = ratingsCase.loadRatings()
    val dateFormat = dateFormatProvider.loadFullDayFormat()
    val translations =
      if (language == Config.DEFAULT_LANGUAGE) emptyMap()
      else translationsRepository.loadAllShowsLocal(language)

    val sortOrder = settingsRepository.sorting.hiddenShowsSortOrder
    val sortType = settingsRepository.sorting.hiddenShowsSortType

    val hiddenItems = showsRepository.hiddenShows.loadAll()
      .map {
        toListItemAsync(
          show = it,
          translation = translations[it.traktId],
          userRating = ratings[it.ids.trakt],
          dateFormat = dateFormat,
          sortOrder = sortOrder,
        )
      }
      .awaitAll()
      .filterByQuery(searchQuery)
      .sortedWith(sorter.sort(sortOrder, sortType))

    if (hiddenItems.isNotEmpty()) {
      val filtersItem = loadFiltersItem(sortOrder, sortType)
      listOf(filtersItem) + hiddenItems
    } else {
      hiddenItems
    }
  }

  private fun List<CollectionListItem.ShowItem>.filterByQuery(query: String) =
    this.filter {
      it.show.title.contains(query, true) ||
        it.translation?.title?.contains(query, true) == true
    }

  private fun loadFiltersItem(
    sortOrder: SortOrder,
    sortType: SortType,
  ): CollectionListItem.FiltersItem {
    return CollectionListItem.FiltersItem(
      sortOrder = sortOrder,
      sortType = sortType,
      isUpcoming = false,
    )
  }

  private fun CoroutineScope.toListItemAsync(
    show: Show,
    translation: Translation?,
    userRating: TraktRating?,
    dateFormat: DateTimeFormatter,
    sortOrder: SortOrder,
  ) = async {
    val image = imagesProvider.findCachedImage(show, ImageType.POSTER)
    CollectionListItem.ShowItem(
      isLoading = false,
      show = show,
      image = image,
      translation = translation,
      userRating = userRating?.rating,
      dateFormat = dateFormat,
      sortOrder = sortOrder,
    )
  }
}
