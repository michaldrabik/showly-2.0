// ktlint-disable max-line-length
package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.michaldrabik.data_local.database.model.Show
import com.michaldrabik.data_local.database.model.WatchlistShow
import com.michaldrabik.data_local.sources.WatchlistShowsLocalDataSource

@Dao
interface WatchlistShowsDao : WatchlistShowsLocalDataSource {

  @Query("SELECT shows.*, shows_see_later.created_at AS created_at, shows_see_later.updated_at AS updated_at FROM shows INNER JOIN shows_see_later USING(id_trakt)")
  override suspend fun getAll(): List<Show>

  @Query("SELECT shows.id_trakt FROM shows INNER JOIN shows_see_later USING(id_trakt)")
  override suspend fun getAllTraktIds(): List<Long>

  @Query("SELECT shows.* FROM shows INNER JOIN shows_see_later ON shows_see_later.id_trakt == shows.id_trakt WHERE shows.id_trakt == :traktId")
  override suspend fun getById(traktId: Long): Show?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  override suspend fun insert(show: WatchlistShow)

  @Query("DELETE FROM shows_see_later WHERE id_trakt == :traktId")
  override suspend fun deleteById(traktId: Long)

  @Query("SELECT EXISTS(SELECT 1 FROM shows_see_later WHERE id_trakt = :traktId LIMIT 1);")
  override suspend fun checkExists(traktId: Long): Boolean
}
