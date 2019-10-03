package com.michaldrabik.showly2.ui.myshows

import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.model.ImageType
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.ShowStatus.CANCELED
import com.michaldrabik.showly2.model.ShowStatus.ENDED
import com.michaldrabik.showly2.model.ShowStatus.IN_PRODUCTION
import com.michaldrabik.showly2.model.ShowStatus.PLANNED
import com.michaldrabik.showly2.model.ShowStatus.RETURNING
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

  companion object {
    private const val RECENT_SHOWS_AMOUNT = 4
  }

  suspend fun loadMyShows(): MyShowBundle {
    val recentShows = database.followedShowsDao().getAllRecent()
      .map { mappers.show.fromDatabase(it) }
      .take(RECENT_SHOWS_AMOUNT)

    val allShows = database.followedShowsDao().getAll()
      .map { mappers.show.fromDatabase(it) }
      .sortedBy { it.title }

    val runningShows = allShows
      .filter { it.status == RETURNING }

    val endedShows = allShows
      .filter { it.status in arrayOf(ENDED, CANCELED) }

    val incomingShows = allShows
      .filter { it.status in arrayOf(IN_PRODUCTION, PLANNED) }

    return MyShowBundle(
      recentShows,
      runningShows,
      endedShows,
      incomingShows
    )
  }

  suspend fun findCachedImage(show: Show, type: ImageType) =
    imagesManager.findCachedImage(show, type)
}