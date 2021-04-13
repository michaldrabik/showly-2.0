package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.michaldrabik.data_local.database.model.Actor

@Dao
interface ActorsDao {

  @Query("SELECT * FROM actors WHERE id_tmdb_show = :tmdbId")
  suspend fun getAllByShow(tmdbId: Long): List<Actor>

  @Query("SELECT * FROM actors WHERE id_tmdb_movie = :tmdbId")
  suspend fun getAllByMovie(tmdbId: Long): List<Actor>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(actors: List<Actor>)

  @Query("DELETE FROM actors WHERE id_tmdb_show = :tmdbId")
  suspend fun deleteAllByShow(tmdbId: Long)

  @Query("DELETE FROM actors WHERE id_tmdb_movie = :tmdbId")
  suspend fun deleteAllByMovie(tmdbId: Long)

  @Transaction
  suspend fun replaceForShow(actors: List<Actor>, tmdbId: Long) {
    deleteAllByShow(tmdbId)
    upsert(actors)
  }

  @Transaction
  suspend fun replaceForMovie(actors: List<Actor>, tmdbId: Long) {
    deleteAllByMovie(tmdbId)
    upsert(actors)
  }
}
