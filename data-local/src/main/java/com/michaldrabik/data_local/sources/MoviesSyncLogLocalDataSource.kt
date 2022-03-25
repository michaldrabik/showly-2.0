package com.michaldrabik.data_local.sources

import com.michaldrabik.data_local.database.model.MoviesSyncLog

interface MoviesSyncLogLocalDataSource {

  suspend fun getAll(): List<MoviesSyncLog>

  suspend fun upsert(log: MoviesSyncLog)
}
