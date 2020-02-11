package com.michaldrabik.storage.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.michaldrabik.storage.database.model.Episode

@Dao
interface EpisodesDao : BaseDao<Episode> {

  @Insert(onConflict = REPLACE)
  suspend fun upsert(episodes: List<Episode>)

  @Query("SELECT * FROM episodes WHERE id_season = :seasonTraktId")
  suspend fun getAllForSeason(seasonTraktId: Long): List<Episode>

  @Query("SELECT * FROM episodes WHERE id_show_trakt IN (:showsIds)")
  suspend fun getAllForShows(showsIds: List<Long>): List<Episode>

  @Query("SELECT * FROM episodes WHERE id_show_trakt IN(:showsIds) AND is_watched = 1")
  suspend fun getAllWatchedForShows(showsIds: List<Long>): List<Episode>

  @Query("SELECT id_trakt FROM episodes WHERE id_show_trakt IN(:showsIds) AND is_watched = 1")
  suspend fun getAllWatchedIdsForShows(showsIds: List<Long>): List<Long>

  @Query("DELETE FROM episodes WHERE id_show_trakt = :showTraktId AND is_watched = 0")
  suspend fun deleteAllUnwatchedForShow(showTraktId: Long)
}
