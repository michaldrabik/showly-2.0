package com.michaldrabik.showly2.ui.followedshows.seelater

import com.michaldrabik.showly2.common.ImagesManager
import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.model.ImageType
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.repository.shows.ShowsRepository
import javax.inject.Inject

@AppScope
class SeeLaterInteractor @Inject constructor(
  private val imagesManager: ImagesManager,
  private val showsRepository: ShowsRepository
) {

  suspend fun loadShows() =
    showsRepository.seeLaterShows.loadAll()
      .sortedBy { it.title }

  suspend fun findCachedImage(show: Show, type: ImageType) =
    imagesManager.findCachedImage(show, type)

  suspend fun loadMissingImage(show: Show, type: ImageType, force: Boolean) =
    imagesManager.loadRemoteImage(show, type, force)
}