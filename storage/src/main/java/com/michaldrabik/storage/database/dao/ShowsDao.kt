package com.michaldrabik.storage.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.michaldrabik.storage.database.model.Show

@Dao
interface ShowsDao {

  @Query("SELECT * FROM shows")
  suspend fun getAll(): List<Show>

  @Query("SELECT * FROM shows WHERE id_trakt IN (:ids)")
  suspend fun getAll(ids: List<Long>): List<Show>

  @Query("SELECT * FROM shows WHERE id_trakt == :traktId")
  suspend fun getById(traktId: Long): Show?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(shows: List<Show>)
}