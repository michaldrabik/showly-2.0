package com.michaldrabik.repository.shows

import androidx.room.withTransaction
import com.michaldrabik.common.Config
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_local.database.model.RelatedShow
import com.michaldrabik.data_remote.Cloud
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Show
import javax.inject.Inject

class RelatedShowsRepository @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val mappers: Mappers
) {

  suspend fun loadAll(show: Show): List<Show> {
    val relatedShows = database.relatedShowsDao().getAllById(show.traktId)
    val latest = relatedShows.maxByOrNull { it.updatedAt }

    if (latest != null && nowUtcMillis() - latest.updatedAt < Config.RELATED_CACHE_DURATION) {
      val relatedShowsIds = relatedShows.map { it.idTrakt }
      return database.showsDao().getAll(relatedShowsIds)
        .map { mappers.show.fromDatabase(it) }
    }

    val remoteShows = cloud.traktApi.fetchRelatedShows(show.traktId)
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
