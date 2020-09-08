package com.michaldrabik.showly2.repository.shows

import com.michaldrabik.showly2.di.scope.AppScope
import com.michaldrabik.showly2.model.IdTrakt
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.showly2.utilities.extensions.nowUtcMillis
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.ArchiveShow
import javax.inject.Inject

@AppScope
class ArchiveShowsRepository @Inject constructor(
  private val database: AppDatabase,
  private val mappers: Mappers
) {

  suspend fun loadAll() =
    database.archiveShowsDao().getAll()
      .map { mappers.show.fromDatabase(it) }

  suspend fun load(id: IdTrakt) =
    database.archiveShowsDao().getById(id.id)?.let {
      mappers.show.fromDatabase(it)
    }

  suspend fun insert(id: IdTrakt) {
    val dbShow = ArchiveShow.fromTraktId(id.id, nowUtcMillis())
    database.archiveShowsDao().insert(listOf(dbShow))
  }

  suspend fun delete(id: IdTrakt) =
    database.archiveShowsDao().deleteById(id.id)
}
