package com.michaldrabik.showly2.ui.myshows

import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.model.ImageType
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.showly2.ui.common.ImagesManager
import com.michaldrabik.storage.database.AppDatabase
import javax.inject.Inject

@AppScope
class MyShowsInteractor @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val imagesManager: ImagesManager,
  private val mappers: Mappers
) {

  suspend fun loadRecentShows(limit: Int = 4): List<Show> {
    val shows = database.followedShowsDao().getAllRecent()
      .map { mappers.show.fromDatabase(it) }
    return shows.take(limit)
  }

  suspend fun findCachedImage(show: Show, type: ImageType) =
    imagesManager.findCachedImage(show, type)
}