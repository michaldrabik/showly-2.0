package com.michaldrabik.showly2.ui.discover

import com.michaldrabik.showly2.common.ImagesManager
import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.model.ImageType
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.repository.shows.ShowsRepository
import javax.inject.Inject

@AppScope
class DiscoverInteractor @Inject constructor(
  private val imagesManager: ImagesManager,
  private val showsRepository: ShowsRepository
) {

  suspend fun isCacheValid() = showsRepository.discoverShows.isCacheValid()

  suspend fun loadCachedShows() = showsRepository.discoverShows.loadAllCached()

  suspend fun loadRemoteShows() = showsRepository.discoverShows.loadAllRemote()

  suspend fun loadMyShowsIds() = showsRepository.myShows.loadAllIds()

  suspend fun findCachedImage(show: Show, type: ImageType) =
    imagesManager.findCachedImage(show, type)

  suspend fun loadMissingImage(show: Show, type: ImageType, force: Boolean) =
    imagesManager.loadRemoteImage(show, type, force)
}