package com.michaldrabik.storage.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.michaldrabik.storage.database.model.Actor

@Dao
interface ActorsDao {

  @Query("SELECT * FROM actors WHERE id_tvdb_show = :tvdbId")
  suspend fun getAllByShow(tvdbId: Long): List<Actor>

  @Query("SELECT * FROM actors WHERE id_tmdb_movie = :tmdbId")
  suspend fun getAllByMovie(tmdbId: Long): List<Actor>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(actors: List<Actor>)

  @Query("DELETE FROM actors WHERE id_tvdb_show = :tvdbId")
  suspend fun deleteAllByShow(tvdbId: Long)

  @Query("DELETE FROM actors WHERE id_tmdb_movie = :tmdbId")
  suspend fun deleteAllByMovie(tmdbId: Long)

  @Transaction
  suspend fun replaceForShow(actors: List<Actor>, tvdbId: Long) {
    deleteAllByShow(tvdbId)
    upsert(actors)
  }

  @Transaction
  suspend fun replaceForMovie(actors: List<Actor>, tmdbId: Long) {
    deleteAllByMovie(tmdbId)
    upsert(actors)
  }
}
