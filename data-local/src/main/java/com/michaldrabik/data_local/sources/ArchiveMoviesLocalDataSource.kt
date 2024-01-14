package com.michaldrabik.data_local.sources

import com.michaldrabik.data_local.database.model.ArchiveMovie
import com.michaldrabik.data_local.database.model.Movie

interface ArchiveMoviesLocalDataSource {

  suspend fun getAll(): List<Movie>

  suspend fun getAll(ids: List<Long>): List<Movie>

  suspend fun getAllTraktIds(): List<Long>

  suspend fun getById(traktId: Long): Movie?

  suspend fun insert(movie: ArchiveMovie)

  suspend fun deleteById(traktId: Long)
}
