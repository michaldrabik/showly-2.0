package com.michaldrabik.storage.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.michaldrabik.storage.database.model.TranslationsSyncLog

@Dao
interface TranslationsSyncLogDao {

  @Query("SELECT * from sync_translations_log")
  suspend fun getAll(): List<TranslationsSyncLog>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(log: TranslationsSyncLog)
}
