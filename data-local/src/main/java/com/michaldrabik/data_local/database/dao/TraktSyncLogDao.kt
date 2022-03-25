package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.michaldrabik.data_local.database.model.TraktSyncLog
import com.michaldrabik.data_local.sources.TraktSyncLogLocalDataSource

@Dao
interface TraktSyncLogDao : TraktSyncLogLocalDataSource {

  @Query("SELECT * FROM sync_trakt_log WHERE type == 'show'")
  override suspend fun getAllShows(): List<TraktSyncLog>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  override suspend fun insert(log: TraktSyncLog)

  @Query("UPDATE sync_trakt_log SET synced_at = :syncedAt WHERE id_trakt == :idTrakt AND type == :type")
  override suspend fun update(idTrakt: Long, type: String, syncedAt: Long): Int

  @Query("DELETE FROM sync_trakt_log")
  override suspend fun deleteAll()

  @Transaction
  override suspend fun upsertShow(idTrakt: Long, syncedAt: Long) {
    val result = update(idTrakt, "show", syncedAt)
    if (result <= 0) {
      insert(TraktSyncLog(0, idTrakt, "show", syncedAt))
    }
  }
}
