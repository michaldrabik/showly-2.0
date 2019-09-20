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
import com.michaldrabik.storage.database.model.TrendingShow
import java.lang.System.currentTimeMillis
import javax.inject.Inject

@AppScope
class DiscoverInteractor @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val imagesManager: ImagesManager,
  private val mappers: Mappers
) {

  suspend fun loadTrendingShows(skipCache: Boolean = false): List<Show> {
    val stamp = database.trendingShowsDao().getMostRecent()?.createdAt ?: 0
    if (!skipCache && currentTimeMillis() - stamp < Config.TRENDING_SHOWS_CACHE_DURATION) {
      return database.trendingShowsDao().getAll().map { mappers.show.fromDatabase(it) }
    }

    val remoteShows = cloud.traktApi.fetchTrendingShows().map { mappers.show.fromNetwork(it) }
    database.withTransaction {
      val timestamp = currentTimeMillis()
      database.showsDao().upsert(remoteShows.map { mappers.show.toDatabase(it) })
      database.trendingShowsDao().deleteAllAndInsert(remoteShows.map {
        TrendingShow(idTrakt = it.ids.trakt, createdAt = timestamp, updatedAt = timestamp)
      })
    }

    return remoteShows
  }

  suspend fun findCachedImage(show: Show, type: ImageType) =
    imagesManager.findCachedImage(show, type)

  suspend fun loadMissingImage(show: Show, type: ImageType, force: Boolean) =
    imagesManager.loadRemoteImage(show, type, force)
}