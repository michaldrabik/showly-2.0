package com.michaldrabik.repository.movies

import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_local.database.model.ArchiveMovie
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_model.IdTrakt
import javax.inject.Inject

class HiddenMoviesRepository @Inject constructor(
  private val database: AppDatabase,
  private val mappers: Mappers
) {

  suspend fun loadAll() =
    database.archiveMoviesDao().getAll()
      .map { mappers.movie.fromDatabase(it) }

  suspend fun loadAll(ids: List<IdTrakt>) =
    database.archiveMoviesDao().getAll(ids.map { it.id })
      .map { mappers.movie.fromDatabase(it) }

  suspend fun load(id: IdTrakt) =
    database.archiveMoviesDao().getById(id.id)?.let {
      mappers.movie.fromDatabase(it)
    }

  suspend fun loadAllIds() = database.archiveMoviesDao().getAllTraktIds()

  suspend fun insert(id: IdTrakt) {
    val dbMovie = ArchiveMovie.fromTraktId(id.id, nowUtcMillis())
    database.archiveMoviesDao().insert(dbMovie)
  }

  suspend fun delete(id: IdTrakt) =
    database.archiveMoviesDao().deleteById(id.id)

  suspend fun exists(id: IdTrakt) =
    database.archiveMoviesDao().getById(id.id) != null
}
