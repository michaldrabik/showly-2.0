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

  fun filterNetworks(
    item: CollectionListItem,
    networks: List<String>,
  ): Boolean {
    if (networks.isEmpty()) {
      return true
    }
    return item.show.network in networks
  }

  fun filterGenres(
    item: CollectionListItem,
    genres: List<String>,
  ): Boolean {
    if (genres.isEmpty()) {
      return true
    }
    return item.show.genres.any { genre -> genre.lowercase() in genres }
  }

  fun filterByQuery(
    item: CollectionListItem.ShowItem,
    query: String,
  ): Boolean {
    return item.show.title.contains(query, true) ||
      item.translation?.title?.contains(query, true) == true
  }
}
