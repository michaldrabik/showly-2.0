package com.michaldrabik.showly2.repository.shows

import androidx.room.withTransaction
import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.Config
import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.model.IdTrakt
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.showly2.utilities.extensions.nowUtcMillis
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.RelatedShow
import javax.inject.Inject

@AppScope
class RelatedShowsRepository @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val mappers: Mappers
) {

  suspend fun loadAll(show: Show): List<Show> {
    val relatedShows = database.relatedShowsDao().getAllById(show.ids.trakt.id)
    val latest = relatedShows.maxBy { it.updatedAt }

    if (latest != null && nowUtcMillis() - latest.updatedAt < Config.RELATED_CACHE_DURATION) {
      val relatedShowsIds = relatedShows.map { it.idTrakt }
      return database.showsDao().getAll(relatedShowsIds)
        .map { mappers.show.fromDatabase(it) }
    }

    val remoteShows = cloud.traktApi.fetchRelatedShows(show.ids.trakt.id)
      .map { mappers.show.fromNetwork(it) }

    cacheRelatedShows(remoteShows, show.ids.trakt)

    return remoteShows
  }

  private suspend fun cacheRelatedShows(shows: List<Show>, showId: IdTrakt) {
    database.withTransaction {
      val timestamp = nowUtcMillis()
      database.showsDao().upsert(shows.map { mappers.show.toDatabase(it) })
      database.relatedShowsDao().deleteById(showId.id)
      database.relatedShowsDao().insert(shows.map { RelatedShow.fromTraktId(it.ids.trakt.id, showId.id, timestamp) })
    }
  }
}