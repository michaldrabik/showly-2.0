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

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(shows: List<Show>)

  @Query("DELETE FROM shows")
  suspend fun deleteAll()
}