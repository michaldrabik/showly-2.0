package com.michaldrabik.ui_repository.shows

import androidx.room.withTransaction
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.WatchlistShow
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_repository.mappers.Mappers
import javax.inject.Inject

@AppScope
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
