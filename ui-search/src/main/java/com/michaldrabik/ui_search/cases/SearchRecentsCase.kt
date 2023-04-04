package com.michaldrabik.ui_search.cases

import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.ui_model.RecentSearch
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject
import com.michaldrabik.data_local.database.model.RecentSearch as RecentSearchDb

@ViewModelScoped
class SearchRecentsCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val localSource: LocalDataSource
) {

  suspend fun getRecentSearches(limit: Int): List<RecentSearch> =
    withContext(dispatchers.IO) {
      localSource.recentSearch.getAll(limit)
        .map { RecentSearch(it.text) }
    }

  suspend fun clearRecentSearches() = withContext(dispatchers.IO) {
    localSource.recentSearch.deleteAll()
  }

  suspend fun saveRecentSearch(query: String) = withContext(dispatchers.IO) {
    val now = nowUtcMillis()
    localSource.recentSearch.upsert(listOf(RecentSearchDb(0, query, now, now)))
  }
}
