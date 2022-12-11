package com.michaldrabik.ui_my_shows.watchlist.helpers

import com.michaldrabik.common.extensions.nowUtc
import com.michaldrabik.ui_my_shows.common.recycler.CollectionListItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchlistItemFilter @Inject constructor() {

  fun filterUpcoming(
    item: CollectionListItem,
    isUpcoming: Boolean,
  ): Boolean {
    if (isUpcoming) {
      val releasedAt = item.getReleaseDate()
      return releasedAt != null && releasedAt.isAfter(nowUtc())
    }
    return true
  }

  fun filterByQuery(
    item: CollectionListItem.ShowItem,
    query: String,
  ): Boolean {
    return item.show.title.contains(query, true) ||
      item.translation?.title?.contains(query, true) == true
  }
}
