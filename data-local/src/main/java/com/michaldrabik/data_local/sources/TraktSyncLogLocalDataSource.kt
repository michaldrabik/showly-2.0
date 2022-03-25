package com.michaldrabik.data_local.sources

import com.michaldrabik.data_local.database.model.TraktSyncLog

interface TraktSyncLogLocalDataSource {

  suspend fun getAllShows(): List<TraktSyncLog>

  suspend fun insert(log: TraktSyncLog)

  suspend fun update(idTrakt: Long, type: String, syncedAt: Long): Int

  suspend fun deleteAll()

  suspend fun upsertShow(idTrakt: Long, syncedAt: Long)
}
