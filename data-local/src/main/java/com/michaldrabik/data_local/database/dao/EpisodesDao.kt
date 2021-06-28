// ktlint-disable max-line-length
package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Transaction
import com.michaldrabik.data_local.database.model.Episode

@Dao
interface EpisodesDao : BaseDao<Episode> {

  @Insert(onConflict = REPLACE)
  suspend fun upsert(episodes: List<Episode>)

  @Transaction
  suspend fun upsertChunked(items: List<Episode>) {
    val chunks = items.chunked(500)
    chunks.forEach { chunk -> upsert(chunk) }
  }

  @Query("SELECT * FROM episodes WHERE id_season = :seasonTraktId")
  suspend fun getAllForSeason(seasonTraktId: Long): List<Episode>

  @Query("SELECT * FROM episodes WHERE id_show_trakt = :showTraktId")
  suspend fun getAllByShowId(showTraktId: Long): List<Episode>

  @Query("SELECT * FROM episodes WHERE id_show_trakt = :showTraktId AND season_number = :seasonNumber")
  suspend fun getAllByShowId(showTraktId: Long, seasonNumber: Int): List<Episode>

  @Query("SELECT * FROM episodes WHERE id_show_trakt IN (:showTraktIds)")
  suspend fun getAllByShowsIds(showTraktIds: List<Long>): List<Episode>

  @Query("SELECT * from episodes where id_show_trakt = :showTraktId AND is_watched = 0 AND season_number != 0 ORDER BY season_number ASC, episode_number ASC LIMIT 1")
  suspend fun getFirstUnwatched(showTraktId: Long): Episode?

  @Query("SELECT * from episodes where id_show_trakt = :showTraktId AND is_watched = 0 AND season_number != 0 AND first_aired <= :toTime ORDER BY season_number ASC, episode_number ASC LIMIT 1")
  suspend fun getFirstUnwatched(showTraktId: Long, toTime: Long): Episode?

  @Query("SELECT * from episodes where id_show_trakt = :showTraktId AND is_watched = 0 AND season_number != 0 AND first_aired > :fromTime AND first_aired <= :toTime ORDER BY season_number ASC, episode_number ASC LIMIT 1")
  suspend fun getFirstUnwatched(showTraktId: Long, fromTime: Long, toTime: Long): Episode?

  @Query("SELECT COUNT(id_trakt) FROM episodes WHERE id_show_trakt = :showTraktId AND first_aired < :toTime AND season_number != 0")
  suspend fun getTotalCount(showTraktId: Long, toTime: Long): Int

  @Query("SELECT COUNT(id_trakt) FROM episodes WHERE id_show_trakt = :showTraktId AND season_number != 0")
  suspend fun getTotalCount(showTraktId: Long): Int

  @Query("SELECT COUNT(id_trakt) FROM episodes WHERE id_show_trakt = :showTraktId AND is_watched = 1 AND first_aired < :toTime AND season_number != 0")
  suspend fun getWatchedCount(showTraktId: Long, toTime: Long): Int

  @Query("SELECT COUNT(id_trakt) FROM episodes WHERE id_show_trakt = :showTraktId AND is_watched = 1 AND season_number != 0")
  suspend fun getWatchedCount(showTraktId: Long): Int

  @Query("SELECT * FROM episodes WHERE id_show_trakt IN(:showsIds) AND is_watched = 1")
  suspend fun getAllWatchedForShows(showsIds: List<Long>): List<Episode>

  @Query("SELECT id_trakt FROM episodes WHERE id_show_trakt IN(:showsIds) AND is_watched = 1")
  suspend fun getAllWatchedIdsForShows(showsIds: List<Long>): List<Long>

  @Query("DELETE FROM episodes WHERE id_show_trakt = :showTraktId AND is_watched = 0")
  suspend fun deleteAllUnwatchedForShow(showTraktId: Long)

  @Query("DELETE FROM episodes WHERE id_show_trakt = :showTraktId")
  suspend fun deleteAllForShow(showTraktId: Long)
}
