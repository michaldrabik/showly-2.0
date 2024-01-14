package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.michaldrabik.data_local.database.model.TranslationsSyncLog
import com.michaldrabik.data_local.sources.TranslationsShowsSyncLogLocalDataSource

@Dao
interface TranslationsSyncLogDao : TranslationsShowsSyncLogLocalDataSource {

  @Query("SELECT * from sync_translations_log")
  override suspend fun getAll(): List<TranslationsSyncLog>

  @Query("SELECT * from sync_translations_log WHERE id_show_trakt == :idTrakt")
  override suspend fun getById(idTrakt: Long): TranslationsSyncLog?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  override suspend fun upsert(log: TranslationsSyncLog)

  @Query("DELETE FROM sync_translations_log")
  override suspend fun deleteAll()
}
