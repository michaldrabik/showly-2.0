package com.michaldrabik.showly2.ui.myshows

import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.model.ImageType
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.ShowStatus.CANCELED
import com.michaldrabik.showly2.model.ShowStatus.ENDED
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

    val runningShows = allShows
      .filter { it.status == RETURNING }
      .sortedBy { it.title }

    val endedShows = allShows
      .filter { it.status in arrayOf(ENDED, CANCELED) }
      .sortedBy { it.title }

    return MyShowBundle(
      recentShows,
      runningShows,
      endedShows
    )
  }

  suspend fun findCachedImage(show: Show, type: ImageType) =
    imagesManager.findCachedImage(show, type)
}