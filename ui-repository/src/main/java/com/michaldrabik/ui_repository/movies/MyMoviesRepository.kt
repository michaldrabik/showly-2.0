package com.michaldrabik.ui_repository.movies

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.MyMovie
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_repository.mappers.Mappers
import javax.inject.Inject

@AppScope
class MyMoviesRepository @Inject constructor(
  private val database: AppDatabase,
  private val mappers: Mappers
) {

  suspend fun load(id: IdTrakt) =
    database.myMoviesDao().getById(id.id)?.let {
      mappers.movie.fromDatabase(it)
    }

  suspend fun loadAll() =
    database.myMoviesDao().getAll()
      .map { mappers.movie.fromDatabase(it) }

  suspend fun loadAll(ids: List<IdTrakt>) =
    database.myMoviesDao().getAll(ids.map { it.id })
      .map { mappers.movie.fromDatabase(it) }

  suspend fun loadAllRecent(amount: Int) =
    database.myMoviesDao().getAllRecent(amount)
      .map { mappers.movie.fromDatabase(it) }

  suspend fun loadAllIds() = database.myMoviesDao().getAllTraktIds()

  suspend fun insert(id: IdTrakt) {
    val timestamp = nowUtcMillis()
    val movie = MyMovie.fromTraktId(id.id, timestamp, timestamp)
    database.myMoviesDao().insert(listOf(movie))
  }

  suspend fun delete(id: IdTrakt) = database.myMoviesDao().deleteById(id.id)
}
