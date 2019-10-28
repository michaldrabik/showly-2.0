package com.michaldrabik.showly2.ui.followedshows.seelater

import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.model.ImageType
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.showly2.ui.common.ImagesManager
import com.michaldrabik.storage.database.AppDatabase
import javax.inject.Inject

@AppScope
class SeeLaterInteractor @Inject constructor(
  private val database: AppDatabase,
  private val imagesManager: ImagesManager,
  private val mappers: Mappers
) {

  suspend fun loadShows() =
    database.watchLaterShowsDao().getAll()
      .sortedBy { it.title }
      .map { mappers.show.fromDatabase(it) }

  suspend fun findCachedImage(show: Show, type: ImageType) =
    imagesManager.findCachedImage(show, type)

  suspend fun loadMissingImage(show: Show, type: ImageType, force: Boolean) =
    imagesManager.loadRemoteImage(show, type, force)
}