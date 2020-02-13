package com.michaldrabik.showly2.ui.search

import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.common.images.ShowImagesProvider
import com.michaldrabik.showly2.di.scope.AppScope
import com.michaldrabik.showly2.model.ImageType
import com.michaldrabik.showly2.model.RecentSearch
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.showly2.repository.shows.ShowsRepository
import com.michaldrabik.showly2.utilities.extensions.nowUtcMillis
import com.michaldrabik.storage.database.AppDatabase
import javax.inject.Inject
import com.michaldrabik.storage.database.model.RecentSearch as RecentSearchDb

@AppScope
class SearchInteractor @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val imagesProvider: ShowImagesProvider,
  private val mappers: Mappers,
  private val showsRepository: ShowsRepository
) {

  suspend fun getRecentSearches(limit: Int): List<RecentSearch> {
    return database.recentSearchDao().getAll(limit)
      .map { RecentSearch(it.text) }
  }

  suspend fun clearRecentSearches() =
    database.recentSearchDao().deleteAll()

  suspend fun searchShows(query: String): List<Show> {
    saveRecentSearch(query)
    val shows = cloud.traktApi.fetchShowsSearch(query)
    return shows.map { mappers.show.fromNetwork(it) }
  }

  private suspend fun saveRecentSearch(query: String) {
    val now = nowUtcMillis()
    database.recentSearchDao().upsert(listOf(RecentSearchDb(0, query, now, now)))
  }

  suspend fun loadMyShowsIds() = showsRepository.myShows.loadAllIds()

  suspend fun findCachedImage(show: Show, type: ImageType) =
    imagesProvider.findCachedImage(show, type)

  suspend fun loadMissingImage(show: Show, type: ImageType, force: Boolean) =
    imagesProvider.loadRemoteImage(show, type, force)
}
