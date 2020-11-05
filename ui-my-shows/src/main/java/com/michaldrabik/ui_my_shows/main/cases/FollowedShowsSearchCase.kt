package com.michaldrabik.ui_my_shows.main.cases

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_my_shows.myshows.recycler.MyShowsItem
import com.michaldrabik.ui_repository.shows.ShowsRepository
import javax.inject.Inject

@AppScope
class FollowedShowsSearchCase @Inject constructor(
  private val showsRepository: ShowsRepository,
  private val imagesProvider: ShowImagesProvider
) {

  private val searchItemsCache = mutableListOf<Show>()

  suspend fun searchFollowed(query: String?): List<MyShowsItem> {
    if (query.isNullOrBlank()) return emptyList()

    if (searchItemsCache.isEmpty()) {
      val collection = showsRepository.loadCollection()
      searchItemsCache.clear()
      searchItemsCache.addAll(collection)
    }

    return searchItemsCache
      .filter { it.title.contains(query, true) || it.network.contains(query, true) }
      .sortedBy { it.title }
      .map {
        val image = imagesProvider.findCachedImage(it, ImageType.FANART)
        MyShowsItem.createSearchItem(it, image)
      }
  }

  fun clearCache() = searchItemsCache.clear()
}
