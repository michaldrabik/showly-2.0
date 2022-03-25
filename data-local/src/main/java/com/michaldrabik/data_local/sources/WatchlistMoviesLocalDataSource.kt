package com.michaldrabik.data_local.sources

import com.michaldrabik.data_local.database.model.Movie
import com.michaldrabik.data_local.database.model.WatchlistMovie

interface WatchlistMoviesLocalDataSource {

  suspend fun getAll(): List<Movie>

  suspend fun getAllTraktIds(): List<Long>

  suspend fun getById(traktId: Long): Movie?

  suspend fun insert(movie: WatchlistMovie)

  suspend fun deleteById(traktId: Long)

  suspend fun checkExists(traktId: Long): Boolean
}
