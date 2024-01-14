package com.michaldrabik.data_local.sources

import com.michaldrabik.data_local.database.model.Movie
import com.michaldrabik.data_local.database.model.MyMovie

interface MyMoviesLocalDataSource {

  suspend fun getAll(): List<Movie>

  suspend fun getAll(ids: List<Long>): List<Movie>

  suspend fun getAllRecent(limit: Int): List<Movie>

  suspend fun getAllTraktIds(): List<Long>

  suspend fun getById(traktId: Long): Movie?

  suspend fun insert(movies: List<MyMovie>)

  suspend fun deleteById(traktId: Long)

  suspend fun checkExists(traktId: Long): Boolean
}
