package com.michaldrabik.showly2.ui.search.cases

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.ui_model.RecentSearch
import javax.inject.Inject
import com.michaldrabik.storage.database.model.RecentSearch as RecentSearchDb

@AppScope
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
