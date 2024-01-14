package com.michaldrabik.data_local.sources

import com.michaldrabik.data_local.database.model.Show
import com.michaldrabik.data_local.database.model.WatchlistShow

interface WatchlistShowsLocalDataSource {

  suspend fun getAll(): List<Show>

  suspend fun getAllTraktIds(): List<Long>

  suspend fun getById(traktId: Long): Show?

  suspend fun insert(show: WatchlistShow)

  suspend fun deleteById(traktId: Long)

  suspend fun checkExists(traktId: Long): Boolean
}
