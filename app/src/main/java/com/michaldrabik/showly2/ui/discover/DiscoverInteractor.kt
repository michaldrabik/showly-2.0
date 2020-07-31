package com.michaldrabik.showly2.ui.discover

import com.michaldrabik.showly2.common.images.ShowImagesProvider
import com.michaldrabik.showly2.di.scope.AppScope
import com.michaldrabik.showly2.model.DiscoverFilters
import com.michaldrabik.showly2.model.ImageType
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.repository.UserTvdbManager
import com.michaldrabik.showly2.repository.settings.SettingsRepository
import com.michaldrabik.showly2.repository.shows.ShowsRepository
import javax.inject.Inject

@AppScope
class DiscoverInteractor @Inject constructor(
  private val imagesProvider: ShowImagesProvider,
  private val tvdbUserManager: UserTvdbManager,
  private val showsRepository: ShowsRepository,
  private val settingsRepository: SettingsRepository
) {

  suspend fun checkTvdbAuth() = tvdbUserManager.checkAuthorization()

  suspend fun isCacheValid() = showsRepository.discoverShows.isCacheValid()

  suspend fun loadCachedShows() = showsRepository.discoverShows.loadAllCached()

  suspend fun loadRemoteShows(filters: DiscoverFilters): List<Show> {
    val showAnticipated = !filters.hideAnticipated
    val genres = filters.genres.toList()
    return showsRepository.discoverShows.loadAllRemote(showAnticipated, genres)
  }

  suspend fun loadMyShowsIds() = showsRepository.myShows.loadAllIds()

  suspend fun loadSeeLaterShowsIds() = showsRepository.seeLaterShows.loadAllIds()

  suspend fun findCachedImage(show: Show, type: ImageType) =
    imagesProvider.findCachedImage(show, type)

  suspend fun loadMissingImage(show: Show, type: ImageType, force: Boolean) =
    imagesProvider.loadRemoteImage(show, type, force)

  suspend fun saveFilters(filters: DiscoverFilters) {
    val settings = settingsRepository.load()
    settingsRepository.update(
      settings.copy(
        discoverFilterFeed = filters.feedOrder,
        discoverFilterGenres = filters.genres,
        showAnticipatedShows = !filters.hideAnticipated
      )
    )
  }

  suspend fun loadFilters(): DiscoverFilters {
    val settings = settingsRepository.load()
    return DiscoverFilters(
      feedOrder = settings.discoverFilterFeed,
      hideAnticipated = !settings.showAnticipatedShows,
      genres = settings.discoverFilterGenres.toList()
    )
  }
}
