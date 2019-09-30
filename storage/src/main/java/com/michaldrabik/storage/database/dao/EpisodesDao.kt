package com.michaldrabik.storage.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.michaldrabik.storage.database.model.Episode

@Dao
interface EpisodesDao {

  @Query("SELECT id_trakt FROM episodes WHERE id_show_trakt = :traktId AND is_watched = 1")
  suspend fun getAllWatchedForShow(traktId: Long): List<Long>

  @Query("SELECT * FROM episodes WHERE id_show_trakt = :traktId")
  suspend fun getAllByShowId(traktId: Long): List<Episode>

  @Query("SELECT * FROM episodes WHERE id_season = :traktId")
  suspend fun getAllForSeason(traktId: Long): List<Episode>

  @Insert(onConflict = REPLACE)
  suspend fun upsert(episode: Episode)

  @Insert(onConflict = REPLACE)
  suspend fun upsert(episodes: List<Episode>)

  @Delete
  suspend fun delete(episode: Episode)

  @Delete
  suspend fun delete(episodes: List<Episode>)

  @Query("DELETE FROM episodes WHERE id_show_trakt = :traktId AND is_watched = 0")
  suspend fun deleteAllUnwatchedForShow(traktId: Long)
}