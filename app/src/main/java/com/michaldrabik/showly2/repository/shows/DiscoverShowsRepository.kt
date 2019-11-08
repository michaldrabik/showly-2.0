package com.michaldrabik.showly2.repository.shows

import androidx.room.withTransaction
import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.Config
import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.showly2.utilities.extensions.nowUtcMillis
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.DiscoverShow
import javax.inject.Inject

@AppScope
class DiscoverShowsRepository @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val mappers: Mappers
) {

  suspend fun loadAll(skipCache: Boolean): List<Show> {
    val stamp = database.discoverShowsDao().getMostRecent()?.createdAt ?: 0
    if (!skipCache && nowUtcMillis() - stamp < Config.DISCOVER_SHOWS_CACHE_DURATION) {
      val cachedShows = database.discoverShowsDao().getAll().map { it.idTrakt }
      val shows = database.showsDao().getAll(cachedShows)

      return cachedShows
        .map { id -> shows.first { it.idTrakt == id } }
        .map { mappers.show.fromDatabase(it) }
    }

    val remoteShows = mutableListOf<Show>()
    val trendingShows = cloud.traktApi.fetchTrendingShows().map { mappers.show.fromNetwork(it) }
    val anticipatedShows = cloud.traktApi.fetchAnticipatedShows().map { mappers.show.fromNetwork(it) }.toMutableList()

    trendingShows.forEachIndexed { index, show ->
      addIfMissing(remoteShows, show)
      if (index % 4 == 0 && anticipatedShows.isNotEmpty()) {
        val element = anticipatedShows.removeAt(0)
        addIfMissing(remoteShows, element)
      }
    }

    cacheDiscoverShows(remoteShows)

    return remoteShows
  }

  private suspend fun cacheDiscoverShows(shows: MutableList<Show>) {
    database.withTransaction {
      val timestamp = nowUtcMillis()
      database.showsDao().upsert(shows.map { mappers.show.toDatabase(it) })
      database.discoverShowsDao().replace(shows.map {
        DiscoverShow(
          idTrakt = it.ids.trakt.id,
          createdAt = timestamp,
          updatedAt = timestamp
        )
      })
    }
  }

  private fun addIfMissing(shows: MutableList<Show>, show: Show) {
    if (shows.none { it.ids.trakt == show.ids.trakt }) {
      shows.add(show)
    }
  }
}