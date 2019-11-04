package com.michaldrabik.showly2.ui.discover

import androidx.room.withTransaction
import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.Config
import com.michaldrabik.showly2.common.ImagesManager
import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.model.Genre
import com.michaldrabik.showly2.model.ImageType
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.showly2.utilities.extensions.nowUtcMillis
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.DiscoverShow
import javax.inject.Inject

@AppScope
class DiscoverInteractor @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val imagesManager: ImagesManager,
  private val mappers: Mappers
) {

  suspend fun loadDiscoverShows(
    genres: List<Genre>,
    skipCache: Boolean = false
  ): List<Show> {

    fun addIfMissing(shows: MutableList<Show>, show: Show) {
      if (shows.none { it.ids.trakt == show.ids.trakt }) {
        shows.add(show)
      }
    }

    val stamp = database.discoverShowsDao().getMostRecent()?.createdAt ?: 0
    if (!skipCache && nowUtcMillis() - stamp < Config.DISCOVER_SHOWS_CACHE_DURATION) {
      return database.discoverShowsDao().getAll()
        .filter { show ->
          when {
            genres.isEmpty() -> true
            else -> genres.any { it.slug in show.genres }
          }
        }
        .map { mappers.show.fromDatabase(it) }
    }

    val discoverShows = mutableListOf<Show>()
    val trendingShows = cloud.traktApi.fetchTrendingShows().map { mappers.show.fromNetwork(it) }
    val anticipatedShows = cloud.traktApi.fetchAnticipatedShows().map { mappers.show.fromNetwork(it) }.toMutableList()

    trendingShows.forEachIndexed { index, show ->
      addIfMissing(discoverShows, show)
      if (index % 4 == 0 && anticipatedShows.isNotEmpty()) {
        val element = anticipatedShows.removeAt(0)
        addIfMissing(discoverShows, element)
      }
    }

    database.withTransaction {
      val timestamp = nowUtcMillis()
      database.showsDao().upsert(discoverShows.map { mappers.show.toDatabase(it) })
      database.discoverShowsDao().deleteAllAndInsert(discoverShows.map {
        DiscoverShow(idTrakt = it.ids.trakt.id, createdAt = timestamp, updatedAt = timestamp)
      })
    }

    return discoverShows
  }

  suspend fun loadFollowedShowsIds() =
    database.followedShowsDao().getAllTraktIds()

  suspend fun findCachedImage(show: Show, type: ImageType) =
    imagesManager.findCachedImage(show, type)

  suspend fun loadMissingImage(show: Show, type: ImageType, force: Boolean) =
    imagesManager.loadRemoteImage(show, type, force)
}