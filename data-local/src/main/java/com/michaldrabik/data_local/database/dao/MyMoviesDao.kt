package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.michaldrabik.data_local.database.model.Movie
import com.michaldrabik.data_local.database.model.MyMovie

@Dao
interface MyMoviesDao {

  @Query("SELECT movies.*, movies_my_movies.updated_at AS updated_at FROM movies INNER JOIN movies_my_movies USING(id_trakt)")
  suspend fun getAll(): List<Movie>

  @Query(
    "SELECT movies.*, movies_my_movies.updated_at AS updated_at FROM movies " +
      "INNER JOIN movies_my_movies USING(id_trakt) WHERE id_trakt IN (:ids)"
  )
  suspend fun getAll(ids: List<Long>): List<Movie>

  @Query("SELECT movies.* FROM movies INNER JOIN movies_my_movies USING(id_trakt) ORDER BY movies_my_movies.updated_at DESC LIMIT :limit")
  suspend fun getAllRecent(limit: Int): List<Movie>

  @Query("SELECT movies.id_trakt FROM movies INNER JOIN movies_my_movies USING(id_trakt)")
  suspend fun getAllTraktIds(): List<Long>

  @Query("SELECT movies.* FROM movies INNER JOIN movies_my_movies USING(id_trakt) WHERE id_trakt == :traktId")
  suspend fun getById(traktId: Long): Movie?

  @Query("UPDATE movies_my_movies SET updated_at = :updatedAt WHERE id_trakt == :traktId")
  suspend fun updateTimestamp(traktId: Long, updatedAt: Long)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(movies: List<MyMovie>)

  @Query("DELETE FROM movies_my_movies WHERE id_trakt == :traktId")
  suspend fun deleteById(traktId: Long)

  @Query("SELECT EXISTS(SELECT 1 FROM movies_my_movies WHERE id_trakt = :traktId LIMIT 1);")
  suspend fun checkExists(traktId: Long): Boolean
}
