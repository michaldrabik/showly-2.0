package com.michaldrabik.storage.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.michaldrabik.storage.database.model.Show
import com.michaldrabik.storage.database.model.WatchLaterShow

@Dao
interface WatchLaterShowsDao {

  @Query("SELECT * FROM shows INNER JOIN shows_watch_later ON shows_watch_later.id_trakt == shows.id_trakt")
  suspend fun getAll(): List<Show>

  @Query("SELECT * FROM shows INNER JOIN shows_watch_later ON shows_watch_later.id_trakt == shows.id_trakt ORDER BY id DESC")
  suspend fun getAllRecent(): List<Show>

  @Query("SELECT * FROM shows INNER JOIN shows_watch_later ON shows_watch_later.id_trakt == shows.id_trakt WHERE shows.id_trakt == :traktId")
  suspend fun getById(traktId: Long): Show?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(show: WatchLaterShow)

  @Query("DELETE FROM shows_watch_later WHERE id_trakt == :traktId")
  suspend fun deleteById(traktId: Long)
}