package com.michaldrabik.showly2.ui.search

import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.model.ImageType
import com.michaldrabik.showly2.model.RecentSearch
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.showly2.ui.common.ImagesInteractor
import com.michaldrabik.storage.database.AppDatabase
import org.threeten.bp.Instant
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId
import javax.inject.Inject
import com.michaldrabik.storage.database.model.RecentSearch as RecentSearchDb

@AppScope
class SearchInteractor @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val imagesInteractor: ImagesInteractor,
  private val mappers: Mappers
) {

  suspend fun getRecentSearches(limit: Int = 10): List<RecentSearch> {
    return database.recentSearchDao().getAll(limit)
      .sortedByDescending { it.createdAt }
      .map {
        val time = OffsetDateTime.ofInstant(Instant.ofEpochMilli(it.createdAt), ZoneId.of("UTC"))
        RecentSearch(it.text, time)
      }
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
    database.recentSearchDao().insert(listOf(RecentSearchDb(0, query, Instant.now().toEpochMilli())))
  }

  suspend fun findCachedImage(show: Show, type: ImageType) =
    imagesInteractor.findCachedImage(show, type)

  suspend fun loadMissingImage(show: Show, type: ImageType, force: Boolean) =
    imagesInteractor.loadRemoteImage(show, type, force)
}