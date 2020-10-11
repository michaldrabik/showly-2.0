package com.michaldrabik.ui_discover.cases

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_discover.recycler.DiscoverListItem
import com.michaldrabik.ui_model.DiscoverFilters
import com.michaldrabik.ui_model.DiscoverSortOrder
import com.michaldrabik.ui_model.DiscoverSortOrder.HOT
import com.michaldrabik.ui_model.DiscoverSortOrder.NEWEST
import com.michaldrabik.ui_model.DiscoverSortOrder.RATING
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_repository.UserTvdbManager
import com.michaldrabik.ui_repository.shows.ShowsRepository
import javax.inject.Inject

@AppScope
class DiscoverShowsCase @Inject constructor(
  private val showsRepository: ShowsRepository,
  private val tvdbUserManager: UserTvdbManager,
  private val imagesProvider: ShowImagesProvider
) {

  suspend fun isCacheValid() = showsRepository.discoverShows.isCacheValid()

  suspend fun loadCachedShows(filters: DiscoverFilters): List<DiscoverListItem> {
    val myShowsIds = showsRepository.myShows.loadAllIds()
    val seeLaterShowsIds = showsRepository.seeLaterShows.loadAllIds()
    val archiveShowsIds = showsRepository.archiveShows.loadAllIds()
    val cachedShows = showsRepository.discoverShows.loadAllCached()

    return prepareShowItems(cachedShows, myShowsIds, seeLaterShowsIds, archiveShowsIds, filters)
  }

  suspend fun loadRemoteShows(filters: DiscoverFilters): List<DiscoverListItem> {
    val showAnticipated = !filters.hideAnticipated
    val genres = filters.genres.toList()

    try {
      tvdbUserManager.checkAuthorization()
    } catch (t: Throwable) {
      // Ignore at this moment
    }

    val myShowsIds = showsRepository.myShows.loadAllIds()
    val seeLaterShowsIds = showsRepository.seeLaterShows.loadAllIds()
    val archiveShowsIds = showsRepository.archiveShows.loadAllIds()
    val remoteShows = showsRepository.discoverShows.loadAllRemote(showAnticipated, genres)

    showsRepository.discoverShows.cacheDiscoverShows(remoteShows)
    return prepareShowItems(remoteShows, myShowsIds, seeLaterShowsIds, archiveShowsIds, filters)
  }

  private suspend fun prepareShowItems(
    shows: List<Show>,
    myShowsIds: List<Long>,
    seeLaterShowsIds: List<Long>,
    archiveShowsIds: List<Long>,
    filters: DiscoverFilters?
  ) = shows
    .filter { !archiveShowsIds.contains(it.traktId) }
    .sortedBy(filters?.feedOrder ?: HOT)
    .mapIndexed { index, show ->
      val itemType = when (index) {
        in (0..500 step 14) -> ImageType.FANART_WIDE
        in (5..500 step 14), in (9..500 step 14) -> ImageType.FANART
        else -> ImageType.POSTER
      }
      val image = imagesProvider.findCachedImage(show, itemType)
      DiscoverListItem(
        show,
        image,
        isFollowed = show.ids.trakt.id in myShowsIds,
        isSeeLater = show.ids.trakt.id in seeLaterShowsIds
      )
    }

  private fun List<Show>.sortedBy(order: DiscoverSortOrder) = when (order) {
    HOT -> this
    RATING -> this.sortedWith(compareByDescending<Show> { it.votes }.thenBy { it.rating })
    NEWEST -> this.sortedByDescending { it.year }
  }
}
