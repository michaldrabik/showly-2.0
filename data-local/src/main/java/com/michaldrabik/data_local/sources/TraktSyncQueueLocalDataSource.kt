package com.michaldrabik.data_local.sources

import com.michaldrabik.data_local.database.model.TraktSyncQueue

interface TraktSyncQueueLocalDataSource {

  suspend fun insert(items: List<TraktSyncQueue>): List<Long>

  suspend fun getAll(): List<TraktSyncQueue>

  suspend fun getAll(types: List<String>): List<TraktSyncQueue>

  suspend fun deleteAll(idsTrakt: List<Long>, type: String): Int

  suspend fun deleteAll(type: String): Int

  suspend fun deleteAllForList(idList: Long): Int

  suspend fun delete(items: List<TraktSyncQueue>)

  suspend fun delete(idTrakt: Long, idList: Long, type: String, operation: String): Int
}
