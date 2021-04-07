package com.michaldrabik.ui_repository.movies

import androidx.room.withTransaction
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.WatchlistMovie
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_repository.mappers.Mappers
import javax.inject.Inject

@AppScope
class WatchlistMoviesRepository @Inject constructor(
  private val database: AppDatabase,
  private val mappers: Mappers,
) {

  suspend fun loadAll() =
    database.watchlistMoviesDao().getAll()
      .map { mappers.movie.fromDatabase(it) }

  suspend fun loadAllIds() = database.watchlistMoviesDao().getAllTraktIds()

  suspend fun load(id: IdTrakt) =
    database.watchlistMoviesDao().getById(id.id)?.let {
      mappers.movie.fromDatabase(it)
    }

  suspend fun insert(id: IdTrakt) {
    val movie = WatchlistMovie.fromTraktId(id.id, nowUtcMillis())
    database.run {
      withTransaction {
        watchlistMoviesDao().insert(movie)
      }
    }
  }

  suspend fun delete(id: IdTrakt) =
    database.watchlistMoviesDao().deleteById(id.id)

  suspend fun exists(id: IdTrakt) =
    database.watchlistMoviesDao().checkExists(id.id)
}
