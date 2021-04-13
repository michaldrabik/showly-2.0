package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.michaldrabik.data_local.database.model.Settings

@Dao
interface SettingsDao {

  @Query("SELECT * FROM settings")
  suspend fun getAll(): Settings

  @Query("SELECT COUNT(*) FROM settings")
  suspend fun getCount(): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(settings: Settings)
}
