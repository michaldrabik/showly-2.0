package com.michaldrabik.repository.shows

import androidx.room.withTransaction
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_local.database.model.WatchlistShow
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_model.IdTrakt
import javax.inject.Inject

class WatchlistShowsRepository @Inject constructor(
  private val database: AppDatabase,
  private val mappers: Mappers,
) {

  suspend fun loadAll() =
    database.watchlistShowsDao().getAll()
      .map { mappers.show.fromDatabase(it) }

  suspend fun loadAllIds() = database.watchlistShowsDao().getAllTraktIds()

  suspend fun load(id: IdTrakt) =
    database.watchlistShowsDao().getById(id.id)?.let {
      mappers.show.fromDatabase(it)
    }

  suspend fun insert(id: IdTrakt) {
    val dbShow = WatchlistShow.fromTraktId(id.id, nowUtcMillis())
    database.run {
      withTransaction {
        watchlistShowsDao().insert(dbShow)
        myShowsDao().deleteById(id.id)
        archiveShowsDao().deleteById(id.id)
      }
    }
  }

  suspend fun delete(id: IdTrakt) =
    database.watchlistShowsDao().deleteById(id.id)

  suspend fun exists(id: IdTrakt) =
    database.watchlistShowsDao().checkExists(id.id)
}
