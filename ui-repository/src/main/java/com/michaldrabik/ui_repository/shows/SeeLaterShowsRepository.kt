package com.michaldrabik.ui_repository.shows

import androidx.room.withTransaction
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.SeeLaterShow
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_repository.mappers.Mappers
import javax.inject.Inject

@AppScope
class SeeLaterShowsRepository @Inject constructor(
  private val database: AppDatabase,
  private val mappers: Mappers
) {

  suspend fun loadAll() =
    database.seeLaterShowsDao().getAll()
      .map { mappers.show.fromDatabase(it) }

  suspend fun loadAllIds() = database.seeLaterShowsDao().getAllTraktIds()

  suspend fun load(id: IdTrakt) =
    database.seeLaterShowsDao().getById(id.id)?.let {
      mappers.show.fromDatabase(it)
    }

  suspend fun insert(id: IdTrakt) {
    val dbShow = SeeLaterShow.fromTraktId(id.id, nowUtcMillis())
    database.run {
      withTransaction {
        seeLaterShowsDao().insert(dbShow)
        myShowsDao().deleteById(id.id)
        archiveShowsDao().deleteById(id.id)
      }
    }
  }

  suspend fun delete(id: IdTrakt) =
    database.seeLaterShowsDao().deleteById(id.id)
}
