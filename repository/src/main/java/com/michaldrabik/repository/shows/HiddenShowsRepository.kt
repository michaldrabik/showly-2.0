package com.michaldrabik.repository.shows

import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_local.database.model.ArchiveShow
import com.michaldrabik.data_local.utilities.TransactionsProvider
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_model.IdTrakt
import javax.inject.Inject

class HiddenShowsRepository @Inject constructor(
  private val localSource: LocalDataSource,
  private val transactions: TransactionsProvider,
  private val mappers: Mappers
) {

  suspend fun loadAll() =
    localSource.archiveShows.getAll()
      .map { mappers.show.fromDatabase(it) }

  suspend fun loadAll(ids: List<IdTrakt>) =
    localSource.archiveShows.getAll(ids.map { it.id })
      .map { mappers.show.fromDatabase(it) }

  suspend fun load(id: IdTrakt) =
    localSource.archiveShows.getById(id.id)?.let {
      mappers.show.fromDatabase(it)
    }

  suspend fun loadAllIds() = localSource.archiveShows.getAllTraktIds()

  suspend fun insert(id: IdTrakt) {
    val dbShow = ArchiveShow.fromTraktId(id.id, nowUtcMillis())
    with(localSource) {
      transactions.withTransaction {
        archiveShows.insert(dbShow)
        myShows.deleteById(id.id)
        watchlistShows.deleteById(id.id)
      }
    }
  }

  suspend fun delete(id: IdTrakt) =
    localSource.archiveShows.deleteById(id.id)

  suspend fun exists(id: IdTrakt) =
    localSource.archiveShows.getById(id.id) != null
}
