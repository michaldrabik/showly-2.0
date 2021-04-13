package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.michaldrabik.data_local.database.model.Movie
import com.michaldrabik.data_local.database.model.WatchlistMovie

@Dao
interface WatchlistMoviesDao {

  @Query("SELECT movies.*, movies_see_later.updated_at FROM movies INNER JOIN movies_see_later USING(id_trakt)")
  suspend fun getAll(): List<Movie>

  @Query("SELECT movies.id_trakt FROM movies INNER JOIN movies_see_later USING(id_trakt)")
  suspend fun getAllTraktIds(): List<Long>

  @Query("SELECT movies.* FROM movies INNER JOIN movies_see_later ON movies_see_later.id_trakt == movies.id_trakt WHERE movies.id_trakt == :traktId")
  suspend fun getById(traktId: Long): Movie?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(movie: WatchlistMovie)

  @Query("DELETE FROM movies_see_later WHERE id_trakt == :traktId")
  suspend fun deleteById(traktId: Long)

  @Query("SELECT EXISTS(SELECT 1 FROM movies_see_later WHERE id_trakt = :traktId LIMIT 1);")
  suspend fun checkExists(traktId: Long): Boolean
}
