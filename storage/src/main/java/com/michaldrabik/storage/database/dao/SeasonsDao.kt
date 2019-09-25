package com.michaldrabik.storage.database.dao

import androidx.room.*
import com.michaldrabik.storage.database.model.Season

@Dao
interface SeasonsDao {

  @Query("SELECT * FROM seasons")
  suspend fun getAll(): List<Season>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(season: Season)

  @Delete
  suspend fun delete(season: Season)
}