package com.michaldrabik.showly2.ui.discover

import androidx.room.withTransaction
import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.Config
import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.model.ImageType
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.showly2.ui.common.ImagesManager
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.DiscoverShow
import java.lang.System.currentTimeMillis
import javax.inject.Inject

@AppScope
class DiscoverInteractor @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val imagesManager: ImagesManager,
  private val mappers: Mappers
) {

  suspend fun loadDiscoverShows(skipCache: Boolean = false): List<Show> {
    val stamp = database.discoverShowsDao().getMostRecent()?.createdAt ?: 0
    if (!skipCache && currentTimeMillis() - stamp < Config.DISCOVER_SHOWS_CACHE_DURATION) {
      return database.discoverShowsDao().getAll().map { mappers.show.fromDatabase(it) }
    }

    val discoverShows = mutableListOf<Show>()
    val trendingShows = cloud.traktApi.fetchTrendingShows().map { mappers.show.fromNetwork(it) }
    val anticipatedShows = cloud.traktApi.fetchAnticipatedShows().map { mappers.show.fromNetwork(it) }.toMutableList()

    trendingShows.forEachIndexed { index, show ->
      discoverShows.add(show)
      if (index % 5 == 0 && anticipatedShows.isNotEmpty()) {
        val element = anticipatedShows.removeAt(0)
        if (!discoverShows.contains(element)) {
          discoverShows.add(element)
        }
      }
    }

    database.withTransaction {
      val timestamp = currentTimeMillis()
      database.showsDao().upsert(discoverShows.map { mappers.show.toDatabase(it) })
      database.discoverShowsDao().deleteAllAndInsert(discoverShows.map {
        DiscoverShow(idTrakt = it.ids.trakt, createdAt = timestamp, updatedAt = timestamp)
      })
    }

    return discoverShows
  }

  suspend fun findCachedImage(show: Show, type: ImageType) =
    imagesManager.findCachedImage(show, type)

  suspend fun loadMissingImage(show: Show, type: ImageType, force: Boolean) =
    imagesManager.loadRemoteImage(show, type, force)
}