package com.michaldrabik.showly2.ui.search

import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.common.ImagesManager
import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.model.ImageType
import com.michaldrabik.showly2.model.RecentSearch
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.showly2.utilities.extensions.nowUtcMillis
import com.michaldrabik.storage.database.AppDatabase
import javax.inject.Inject
import com.michaldrabik.storage.database.model.RecentSearch as RecentSearchDb

@AppScope
class SearchInteractor @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val imagesManager: ImagesManager,
  private val mappers: Mappers
) {

  suspend fun getRecentSearches(limit: Int = 10): List<RecentSearch> {
    return database.recentSearchDao().getAll(limit)
      .sortedByDescending { it.createdAt }
      .map { RecentSearch(it.text) }
  }

  suspend fun clearRecentSearches() {
    database.recentSearchDao().deleteAll()
  }

  suspend fun searchShows(query: String): List<Show> {
    saveRecentSearch(query)
    val shows = cloud.traktApi.fetchShowsSearch(query)
    return shows.map { mappers.show.fromNetwork(it) }
  }

  private suspend fun saveRecentSearch(query: String) {
    val now = nowUtcMillis()
    database.recentSearchDao().insert(listOf(RecentSearchDb(0, query, now, now)))
  }

  suspend fun loadFollowedShowsIds() =
    database.followedShowsDao().getAllTraktIds()

  suspend fun findCachedImage(show: Show, type: ImageType) =
    imagesManager.findCachedImage(show, type)

  suspend fun loadMissingImage(show: Show, type: ImageType, force: Boolean) =
    imagesManager.loadRemoteImage(show, type, force)
}