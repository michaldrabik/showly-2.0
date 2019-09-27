package com.michaldrabik.storage.database.dao

import androidx.room.*
import com.michaldrabik.storage.database.model.Season

@Dao
interface SeasonsDao {

  @Query("SELECT * FROM seasons")
  suspend fun getAll(): List<Season>

  @Query("SELECT * FROM seasons WHERE id_trakt = :traktId")
  suspend fun getById(traktId: Long): Season?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(season: Season)

  @Update
  suspend fun update(season: Season)

  @Delete
  suspend fun delete(season: Season)

  @Query("DELETE FROM seasons WHERE id_show_trakt == :traktId")
  suspend fun deleteAllForShow(traktId: Long)
}