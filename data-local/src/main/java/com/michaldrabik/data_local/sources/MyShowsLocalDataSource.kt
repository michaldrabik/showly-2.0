package com.michaldrabik.data_local.sources

import com.michaldrabik.data_local.database.model.MyShow
import com.michaldrabik.data_local.database.model.Show

interface MyShowsLocalDataSource {

  suspend fun getAll(): List<Show>

  suspend fun getAll(ids: List<Long>): List<Show>

  suspend fun getAllRecent(limit: Int): List<Show>

  suspend fun getAllTraktIds(): List<Long>

  suspend fun getById(traktId: Long): Show?

  suspend fun updateWatchedAt(traktId: Long, watchedAt: Long)

  suspend fun insert(shows: List<MyShow>)

  suspend fun deleteById(traktId: Long)

  suspend fun checkExists(traktId: Long): Boolean
}
