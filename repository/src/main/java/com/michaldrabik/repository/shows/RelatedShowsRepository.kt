package com.michaldrabik.repository.shows

import com.michaldrabik.common.Config
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_local.database.model.RelatedShow
import com.michaldrabik.data_local.utilities.TransactionsProvider
import com.michaldrabik.data_remote.RemoteDataSource
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Show
import javax.inject.Inject
import kotlin.math.min

class RelatedShowsRepository @Inject constructor(
  private val remoteSource: RemoteDataSource,
  private val localSource: LocalDataSource,
  private val transactions: TransactionsProvider,
  private val mappers: Mappers
) {

  suspend fun loadAll(show: Show, hiddenCount: Int): List<Show> {
    val relatedShows = localSource.relatedShows.getAllById(show.traktId)
    val latest = relatedShows.maxByOrNull { it.updatedAt }

    if (latest != null && nowUtcMillis() - latest.updatedAt < Config.RELATED_CACHE_DURATION) {
      val relatedShowsIds = relatedShows.map { it.idTrakt }
      return localSource.shows.getAll(relatedShowsIds)
        .map { mappers.show.fromDatabase(it) }
    }

    val remoteShows = remoteSource.trakt.fetchRelatedShows(show.traktId, min(hiddenCount, 10))
      .map { mappers.show.fromNetwork(it) }

    cacheRelatedShows(remoteShows, show.ids.trakt)

    return remoteShows
  }

  private suspend fun cacheRelatedShows(shows: List<Show>, showId: IdTrakt) {
    transactions.withTransaction {
      val timestamp = nowUtcMillis()
      localSource.shows.upsert(shows.map { mappers.show.toDatabase(it) })
      localSource.relatedShows.deleteById(showId.id)
      localSource.relatedShows.insert(
        shows.map {
          RelatedShow.fromTraktId(it.ids.trakt.id, showId.id, timestamp)
        }
      )
    }
  }
}
