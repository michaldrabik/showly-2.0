package com.michaldrabik.storage.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.michaldrabik.storage.database.model.Movie

@Dao
interface MoviesDao : BaseDao<Movie> {

  @Query("SELECT * FROM movies")
  suspend fun getAll(): List<Movie>

  @Query("SELECT * FROM movies WHERE id_trakt IN (:ids)")
  suspend fun getAll(ids: List<Long>): List<Movie>

  @Query("SELECT * FROM movies WHERE id_trakt == :traktId")
  suspend fun getById(traktId: Long): Movie?

  @Transaction
  suspend fun upsert(movies: List<Movie>) {
    val result = insert(movies)

    val updateList = mutableListOf<Movie>()
    result.forEachIndexed { index, id ->
      if (id == -1L) updateList.add(movies[index])
    }

    if (updateList.isNotEmpty()) update(updateList)
  }
}
