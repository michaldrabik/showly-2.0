package com.michaldrabik.repository.movies

import androidx.room.withTransaction
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_local.database.model.WatchlistMovie
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_model.IdTrakt
import javax.inject.Inject

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
