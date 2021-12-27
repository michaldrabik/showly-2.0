package com.michaldrabik.repository.shows

import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_local.database.model.ArchiveShow
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_model.IdTrakt
import javax.inject.Inject

class HiddenShowsRepository @Inject constructor(
  private val database: AppDatabase,
  private val mappers: Mappers
) {

  suspend fun loadAll() =
    database.archiveShowsDao().getAll()
      .map { mappers.show.fromDatabase(it) }

  suspend fun loadAll(ids: List<IdTrakt>) =
    database.archiveShowsDao().getAll(ids.map { it.id })
      .map { mappers.show.fromDatabase(it) }

  suspend fun load(id: IdTrakt) =
    database.archiveShowsDao().getById(id.id)?.let {
      mappers.show.fromDatabase(it)
    }

  suspend fun loadAllIds() = database.archiveShowsDao().getAllTraktIds()

  suspend fun insert(id: IdTrakt) {
    val dbShow = ArchiveShow.fromTraktId(id.id, nowUtcMillis())
    database.archiveShowsDao().insert(dbShow)
  }

  suspend fun delete(id: IdTrakt) =
    database.archiveShowsDao().deleteById(id.id)

  suspend fun exists(id: IdTrakt) =
    database.archiveShowsDao().getById(id.id) != null
}
