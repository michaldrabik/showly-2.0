package com.michaldrabik.ui_search.cases

import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.ui_model.RecentSearch
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject
import com.michaldrabik.data_local.database.model.RecentSearch as RecentSearchDb

@ViewModelScoped
class SearchRecentsCase @Inject constructor(
  private val database: AppDatabase
) {

  suspend fun getRecentSearches(limit: Int): List<RecentSearch> {
    return database.recentSearchDao().getAll(limit)
      .map { RecentSearch(it.text) }
  }

  suspend fun clearRecentSearches() =
    database.recentSearchDao().deleteAll()

  suspend fun saveRecentSearch(query: String) {
    val now = nowUtcMillis()
    database.recentSearchDao().upsert(listOf(RecentSearchDb(0, query, now, now)))
  }
}
