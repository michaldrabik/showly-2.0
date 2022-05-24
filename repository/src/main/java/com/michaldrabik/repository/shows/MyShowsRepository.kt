package com.michaldrabik.repository.shows

import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_local.database.model.MyShow
import com.michaldrabik.data_local.utilities.TransactionsProvider
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_model.IdTrakt
import javax.inject.Inject

class MyShowsRepository @Inject constructor(
  private val localSource: LocalDataSource,
  private val transactions: TransactionsProvider,
  private val mappers: Mappers,
) {

  suspend fun load(id: IdTrakt) =
    localSource.myShows.getById(id.id)?.let {
      mappers.show.fromDatabase(it)
    }

  suspend fun loadAll() =
    localSource.myShows.getAll()
      .map { mappers.show.fromDatabase(it) }

  suspend fun loadAll(ids: List<IdTrakt>) =
    localSource.myShows.getAll(ids.map { it.id })
      .map { mappers.show.fromDatabase(it) }

  suspend fun loadAllRecent(amount: Int) =
    localSource.myShows.getAllRecent(amount)
      .map { mappers.show.fromDatabase(it) }

  suspend fun loadAllIds() = localSource.myShows.getAllTraktIds()

  suspend fun insert(id: IdTrakt, lastWatchedAt: Long) {
    val nowUtc = nowUtcMillis()
    val dbShow = MyShow.fromTraktId(
      traktId = id.id,
      createdAt = nowUtc,
      updatedAt = nowUtc,
      watchedAt = lastWatchedAt
    )
    with(localSource) {
      transactions.withTransaction {
        myShows.insert(listOf(dbShow))
        watchlistShows.deleteById(id.id)
        archiveShows.deleteById(id.id)
      }
    }
  }

  suspend fun delete(id: IdTrakt) =
    localSource.myShows.deleteById(id.id)

  suspend fun exists(id: IdTrakt) =
    localSource.myShows.checkExists(id.id)
}
