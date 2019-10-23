package com.michaldrabik.storage.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.michaldrabik.storage.database.model.Season

@Dao
interface SeasonsDao {

  @Query("SELECT id_trakt FROM seasons WHERE id_show_trakt = :traktId AND is_watched = 1")
  suspend fun getAllWatchedForShow(traktId: Long): List<Long>

  @Query("SELECT * FROM seasons WHERE id_show_trakt = :traktId")
  suspend fun getAllByShowId(traktId: Long): List<Season>

  @Query("SELECT * FROM seasons WHERE id_trakt = :traktId")
  suspend fun getById(traktId: Long): Season?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(season: Season)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(seasons: List<Season>)

  @Update
  suspend fun update(season: Season)

  @Delete
  suspend fun delete(season: Season)

  @Delete
  suspend fun delete(seasons: List<Season>)
}