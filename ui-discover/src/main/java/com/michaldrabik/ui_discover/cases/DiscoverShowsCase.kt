package com.michaldrabik.ui_discover.cases

import com.michaldrabik.common.Config
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_discover.recycler.DiscoverListItem
import com.michaldrabik.ui_model.DiscoverFilters
import com.michaldrabik.ui_model.DiscoverSortOrder
import com.michaldrabik.ui_model.DiscoverSortOrder.HOT
import com.michaldrabik.ui_model.DiscoverSortOrder.NEWEST
import com.michaldrabik.ui_model.DiscoverSortOrder.RATING
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Show
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

@ViewModelScoped
class DiscoverShowsCase @Inject constructor(
  private val showsRepository: ShowsRepository,
  private val imagesProvider: ShowImagesProvider,
  private val translationsRepository: TranslationsRepository
) {

  suspend fun isCacheValid() = showsRepository.discoverShows.isCacheValid()

  suspend fun loadCachedShows(filters: DiscoverFilters) = coroutineScope {
    val myShowsIds = async { showsRepository.myShows.loadAllIds() }
    val watchlistShowsIds = async { showsRepository.watchlistShows.loadAllIds() }
    val archiveShowsIds = async { showsRepository.archiveShows.loadAllIds() }
    val cachedShows = async { showsRepository.discoverShows.loadAllCached() }
    val language = translationsRepository.getLanguage()

    prepareItems(
      cachedShows.await(),
      myShowsIds.await(),
      watchlistShowsIds.await(),
      archiveShowsIds.await(),
      filters,
      language
    )
  }

  suspend fun loadRemoteShows(filters: DiscoverFilters): List<DiscoverListItem> {
    val showAnticipated = !filters.hideAnticipated
    val showCollection = !filters.hideCollection
    val genres = filters.genres.toList()

    val myShowsIds = showsRepository.myShows.loadAllIds()
    val watchlistShowsIds = showsRepository.watchlistShows.loadAllIds()
    val archiveShowsIds = showsRepository.archiveShows.loadAllIds()
    val collectionSize = myShowsIds.size + watchlistShowsIds.size + archiveShowsIds.size

    val remoteShows = showsRepository.discoverShows.loadAllRemote(showAnticipated, showCollection, collectionSize, genres)
    val language = translationsRepository.getLanguage()

    showsRepository.discoverShows.cacheDiscoverShows(remoteShows)
    return prepareItems(remoteShows, myShowsIds, watchlistShowsIds, archiveShowsIds, filters, language)
  }

  private suspend fun prepareItems(
    shows: List<Show>,
    myShowsIds: List<Long>,
    watchlistShowsIds: List<Long>,
    archiveShowsIds: List<Long>,
    filters: DiscoverFilters?,
    language: String
  ) = coroutineScope {
    val collectionIds = myShowsIds + watchlistShowsIds + archiveShowsIds
    shows
      .filter { !archiveShowsIds.contains(it.traktId) }
      .filter {
        if (filters?.hideCollection == false) true
        else !collectionIds.contains(it.traktId)
      }
      .sortedBy(filters?.feedOrder ?: HOT)
      .mapIndexed { index, show ->
        async {
          val itemType = when (index) {
            in (0..500 step 14) -> ImageType.FANART_WIDE
            in (5..500 step 14), in (9..500 step 14) -> ImageType.FANART
            else -> ImageType.POSTER
          }
          val image = imagesProvider.findCachedImage(show, itemType)
          val translation = loadTranslation(language, itemType, show)
          DiscoverListItem(
            show,
            image,
            isFollowed = show.traktId in myShowsIds,
            isWatchlist = show.traktId in watchlistShowsIds,
            translation = translation
          )
        }
      }.awaitAll()
  }

  private suspend fun loadTranslation(language: String, itemType: ImageType, show: Show) =
    if (language == Config.DEFAULT_LANGUAGE || itemType == ImageType.POSTER) null
    else translationsRepository.loadTranslation(show, language, true)

  private fun List<Show>.sortedBy(order: DiscoverSortOrder) = when (order) {
    HOT -> this
    RATING -> this.sortedWith(compareByDescending<Show> { it.votes }.thenBy { it.rating })
    NEWEST -> this.sortedByDescending { it.year }
  }
}
