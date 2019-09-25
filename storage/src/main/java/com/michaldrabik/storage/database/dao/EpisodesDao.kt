package com.michaldrabik.storage.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.michaldrabik.storage.database.model.Episode

@Dao
interface EpisodesDao {

  @Query("SELECT * FROM episodes")
  suspend fun getAll(): List<Episode>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(episode: Episode)

  @Query("DELETE FROM episodes WHERE id == :id")
  suspend fun delete(id: Long)
}