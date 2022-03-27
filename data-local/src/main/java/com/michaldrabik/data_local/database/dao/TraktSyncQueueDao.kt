package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.michaldrabik.data_local.database.model.TraktSyncQueue
import com.michaldrabik.data_local.sources.TraktSyncQueueLocalDataSource

@Dao
interface TraktSyncQueueDao : TraktSyncQueueLocalDataSource {

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  override suspend fun insert(items: List<TraktSyncQueue>): List<Long>

  @Delete
  override suspend fun delete(items: List<TraktSyncQueue>)

  @Query("SELECT * FROM trakt_sync_queue ORDER BY created_at ASC")
  override suspend fun getAll(): List<TraktSyncQueue>

  @Query("SELECT * FROM trakt_sync_queue WHERE type IN (:types) ORDER BY created_at ASC")
  override suspend fun getAll(types: List<String>): List<TraktSyncQueue>

  @Query("DELETE FROM trakt_sync_queue WHERE id_trakt IN (:idsTrakt) AND type = :type")
  override suspend fun deleteAll(idsTrakt: List<Long>, type: String): Int

  @Query("DELETE FROM trakt_sync_queue WHERE type = :type")
  override suspend fun deleteAll(type: String): Int

  @Query("DELETE FROM trakt_sync_queue WHERE id_list = :idList")
  override suspend fun deleteAllForList(idList: Long): Int

  @Query("DELETE FROM trakt_sync_queue WHERE id_trakt = :idTrakt AND id_list = :idList AND type = :type AND operation = :operation")
  override suspend fun delete(
    idTrakt: Long,
    idList: Long,
    type: String,
    operation: String
  ): Int
}
