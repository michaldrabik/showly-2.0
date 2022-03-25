package com.michaldrabik.data_local.sources

import com.michaldrabik.data_local.database.model.RecentSearch

interface RecentSearchLocalDataSource {

  suspend fun getAll(limit: Int): List<RecentSearch>

  suspend fun upsert(searches: List<RecentSearch>)

  suspend fun deleteAll()
}
