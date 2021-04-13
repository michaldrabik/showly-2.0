package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.michaldrabik.data_local.database.model.TraktSyncQueue

@Dao
interface TraktSyncQueueDao : BaseDao<TraktSyncQueue> {

  @Query("SELECT * FROM trakt_sync_queue ORDER BY created_at ASC")
  suspend fun getAll(): List<TraktSyncQueue>

  @Query("SELECT * FROM trakt_sync_queue WHERE type IN (:types) ORDER BY created_at ASC")
  suspend fun getAll(types: List<String>): List<TraktSyncQueue>

  @Query("DELETE FROM trakt_sync_queue WHERE id_trakt IN (:idsTrakt) AND type = :type")
  suspend fun deleteAll(idsTrakt: List<Long>, type: String): Int

  @Query("DELETE FROM trakt_sync_queue WHERE id_list = :idList")
  suspend fun deleteAllForList(idList: Long): Int

  @Query("DELETE FROM trakt_sync_queue WHERE id_trakt = :idTrakt AND id_list = :idList AND type = :type AND operation = :operation")
  suspend fun delete(
    idTrakt: Long,
    idList: Long,
    type: String,
    operation: String
  ): Int
}
