package com.michaldrabik.storage.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.michaldrabik.storage.database.model.Episode

@Dao
interface EpisodesDao {

  @Query("SELECT * FROM episodes")
  suspend fun getAll(): List<Episode>

  @Query("SELECT * FROM episodes WHERE id_show_trakt = :traktId")
  suspend fun getAllForShow(traktId: Long): List<Episode>

  @Query("SELECT * FROM episodes WHERE id_season = :traktId")
  suspend fun getAllForSeason(traktId: Long): List<Episode>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(episode: Episode)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(episodes: List<Episode>)

  @Query("DELETE FROM episodes WHERE id_trakt == :id")
  suspend fun delete(id: Long)
}