package com.michaldrabik.ui_repository.shows

import androidx.room.withTransaction
import com.michaldrabik.common.Config
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.network.Cloud
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.RelatedShow
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_repository.mappers.Mappers
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
