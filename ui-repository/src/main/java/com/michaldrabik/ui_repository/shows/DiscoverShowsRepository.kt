package com.michaldrabik.ui_repository.shows

import androidx.room.withTransaction
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.network.Cloud
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.DiscoverShow
import com.michaldrabik.ui_model.Genre
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_repository.Config
import com.michaldrabik.ui_repository.mappers.Mappers
import javax.inject.Inject

@AppScope
class DiscoverShowsRepository @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val mappers: Mappers
) {

  suspend fun isCacheValid(): Boolean {
    val stamp = database.discoverShowsDao().getMostRecent()?.createdAt ?: 0
    return nowUtcMillis() - stamp < Config.DISCOVER_SHOWS_CACHE_DURATION
  }

  suspend fun loadAllCached(): List<Show> {
    val cachedShows = database.discoverShowsDao().getAll().map { it.idTrakt }
    val shows = database.showsDao().getAll(cachedShows)

    return cachedShows
      .map { id -> shows.first { it.idTrakt == id } }
      .map { mappers.show.fromDatabase(it) }
  }

  suspend fun loadAllRemote(
    showAnticipated: Boolean,
    genres: List<Genre>
  ): List<Show> {
    val remoteShows = mutableListOf<Show>()
    val anticipatedShows = mutableListOf<Show>()
    val popularShows = mutableListOf<Show>()
    val genresQuery = genres.joinToString(",") { it.slug }

    val trendingShows = cloud.traktApi.fetchTrendingShows(genresQuery).map { mappers.show.fromNetwork(it) }

    if (genres.isNotEmpty()) {
      // Wa are adding popular results for genres filtered content to add more results.
      val popular = cloud.traktApi.fetchPopularShows(genresQuery).map { mappers.show.fromNetwork(it) }
      popularShows.addAll(popular)
    }

    if (showAnticipated) {
      val shows = cloud.traktApi.fetchAnticipatedShows(genresQuery).map { mappers.show.fromNetwork(it) }.toMutableList()
      anticipatedShows.addAll(shows)
    }

    trendingShows.forEachIndexed { index, show ->
      addIfMissing(remoteShows, show)
      if (index % 4 == 0 && anticipatedShows.isNotEmpty()) {
        val element = anticipatedShows.removeAt(0)
        addIfMissing(remoteShows, element)
      }
    }
    popularShows.forEach { show -> addIfMissing(remoteShows, show) }

    return remoteShows
  }

  suspend fun cacheDiscoverShows(shows: List<Show>) {
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
    if (shows.any { it.ids.trakt == show.ids.trakt }) return
    shows.add(show)
  }
}
