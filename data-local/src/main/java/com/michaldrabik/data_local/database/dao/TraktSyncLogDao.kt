package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.michaldrabik.data_local.database.model.TraktSyncLog

@Dao
interface TraktSyncLogDao {

  @Query("SELECT * FROM sync_trakt_log WHERE type == 'show'")
  suspend fun getAllShows(): List<TraktSyncLog>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(log: TraktSyncLog)

  @Query("UPDATE sync_trakt_log SET synced_at = :syncedAt WHERE id_trakt == :idTrakt AND type == :type")
  suspend fun update(idTrakt: Long, type: String, syncedAt: Long): Int

  @Query("DELETE FROM sync_trakt_log")
  suspend fun deleteAll()

  @Transaction
  suspend fun upsertShow(idTrakt: Long, syncedAt: Long) {
    val result = update(idTrakt, "show", syncedAt)
    if (result <= 0) {
      insert(TraktSyncLog(0, idTrakt, "show", syncedAt))
    }
  }
}
