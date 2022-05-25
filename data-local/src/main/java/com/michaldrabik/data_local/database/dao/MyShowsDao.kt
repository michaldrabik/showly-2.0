// ktlint-disable max-line-length
package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.michaldrabik.data_local.database.model.MyShow
import com.michaldrabik.data_local.database.model.Show
import com.michaldrabik.data_local.sources.MyShowsLocalDataSource

@Dao
interface MyShowsDao : MyShowsLocalDataSource {

  @Query("SELECT shows.*, shows_my_shows.created_at AS created_at, shows_my_shows.last_watched_at AS updated_at FROM shows INNER JOIN shows_my_shows USING(id_trakt)")
  override suspend fun getAll(): List<Show>

  @Query("SELECT shows.*, shows_my_shows.created_at AS created_at, shows_my_shows.last_watched_at AS updated_at FROM shows INNER JOIN shows_my_shows USING(id_trakt) WHERE id_trakt IN (:ids)")
  override suspend fun getAll(ids: List<Long>): List<Show>

  @Query("SELECT shows.* FROM shows INNER JOIN shows_my_shows USING(id_trakt) ORDER BY shows_my_shows.created_at DESC LIMIT :limit")
  override suspend fun getAllRecent(limit: Int): List<Show>

  @Query("SELECT shows.id_trakt FROM shows INNER JOIN shows_my_shows USING(id_trakt)")
  override suspend fun getAllTraktIds(): List<Long>

  @Query("SELECT shows.* FROM shows INNER JOIN shows_my_shows USING(id_trakt) WHERE id_trakt == :traktId")
  override suspend fun getById(traktId: Long): Show?

  @Query("UPDATE shows_my_shows SET last_watched_at = :watchedAt WHERE id_trakt == :traktId")
  override suspend fun updateWatchedAt(traktId: Long, watchedAt: Long)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  override suspend fun insert(shows: List<MyShow>)

  @Query("DELETE FROM shows_my_shows WHERE id_trakt == :traktId")
  override suspend fun deleteById(traktId: Long)

  @Query("SELECT EXISTS(SELECT 1 FROM shows_my_shows WHERE id_trakt = :traktId LIMIT 1);")
  override suspend fun checkExists(traktId: Long): Boolean
}
