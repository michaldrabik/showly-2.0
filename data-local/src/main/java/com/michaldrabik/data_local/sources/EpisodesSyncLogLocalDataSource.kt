package com.michaldrabik.data_local.sources

import com.michaldrabik.data_local.database.model.EpisodesSyncLog

interface EpisodesSyncLogLocalDataSource {

  suspend fun getAll(): List<EpisodesSyncLog>

  suspend fun upsert(log: EpisodesSyncLog)
}
