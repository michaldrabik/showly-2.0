package com.michaldrabik.storage.database.dao

import androidx.room.*
import com.michaldrabik.storage.database.model.Episode

@Dao
interface EpisodesDao {

  @Query("SELECT * FROM episodes")
  suspend fun getAll(): List<Episode>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(episode: Episode)

  @Delete
  suspend fun delete(episode: Episode)
}