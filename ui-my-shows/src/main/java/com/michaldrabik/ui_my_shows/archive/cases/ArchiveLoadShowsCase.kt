package com.michaldrabik.ui_my_shows.archive.cases

import com.michaldrabik.common.Config
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.images.ShowImagesProvider
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.TraktRating
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_my_shows.archive.helpers.ArchiveItemSorter
import com.michaldrabik.ui_my_shows.archive.recycler.ArchiveListItem
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

@ViewModelScoped
class ArchiveLoadShowsCase @Inject constructor(
  private val ratingsCase: ArchiveRatingsCase,
  private val sorter: ArchiveItemSorter,
  private val showsRepository: ShowsRepository,
  private val translationsRepository: TranslationsRepository,
  private val settingsRepository: SettingsRepository,
  private val imagesProvider: ShowImagesProvider,
) {

  val language by lazy { translationsRepository.getLanguage() }

  suspend fun loadShows(searchQuery: String): List<ArchiveListItem> = coroutineScope {
    val ratings = ratingsCase.loadRatings()
    val translations =
      if (language == Config.DEFAULT_LANGUAGE) emptyMap()
      else translationsRepository.loadAllShowsLocal(language)

    val sortOrder = settingsRepository.sorting.hiddenShowsSortOrder
    val sortType = settingsRepository.sorting.hiddenShowsSortType

    showsRepository.hiddenShows.loadAll()
      .map {
        toListItemAsync(
          show = it,
          translation = translations[it.traktId],
          userRating = ratings[it.ids.trakt]
        )
      }
      .awaitAll()
      .filterByQuery(searchQuery)
      .sortedWith(sorter.sort(sortOrder, sortType))
  }

  private fun List<ArchiveListItem>.filterByQuery(query: String) =
    this.filter {
      it.show.title.contains(query, true) ||
        it.translation?.title?.contains(query, true) == true
    }

  private fun CoroutineScope.toListItemAsync(
    show: Show,
    translation: Translation?,
    userRating: TraktRating?,
  ) = async {
    val image = imagesProvider.findCachedImage(show, ImageType.POSTER)
    ArchiveListItem(
      isLoading = false,
      show = show,
      image = image,
      translation = translation,
      userRating = userRating?.rating
    )
  }
}
