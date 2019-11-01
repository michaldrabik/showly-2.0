package com.michaldrabik.showly2.ui.followedshows

import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.model.ImageType
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.showly2.ui.common.ImagesManager
import com.michaldrabik.storage.database.AppDatabase
import javax.inject.Inject

@AppScope
class FollowedShowsInteractor @Inject constructor(
  private val database: AppDatabase,
  private val imagesManager: ImagesManager,
  private val mappers: Mappers
) {

  private val searchItemsCache = mutableListOf<Show>()

  suspend fun searchFollowed(query: String?): List<Show> {
    if (query.isNullOrBlank()) return emptyList()

    if (searchItemsCache.isEmpty()) {
      val seeLaterShows = database.seeLaterShowsDao().getAll()
      val myShows = database.followedShowsDao().getAll()

      val allShows = (seeLaterShows + myShows)
        .map { mappers.show.fromDatabase(it) }

      searchItemsCache.clear()
      searchItemsCache.addAll(allShows)
    }

    return searchItemsCache
      .filter {
        it.title.contains(query, true) || it.network.contains(query, true)
      }
      .sortedBy { it.title }
  }

  suspend fun findCachedImage(show: Show, type: ImageType) =
    imagesManager.findCachedImage(show, type)

  suspend fun loadMissingImage(show: Show, type: ImageType, force: Boolean) =
    imagesManager.loadRemoteImage(show, type, force)

  fun clearCache() = searchItemsCache.clear()
}