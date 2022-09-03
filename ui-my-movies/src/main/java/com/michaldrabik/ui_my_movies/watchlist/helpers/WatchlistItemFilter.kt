package com.michaldrabik.ui_my_movies.watchlist.helpers

import com.michaldrabik.common.extensions.nowUtcDay
import com.michaldrabik.ui_my_movies.watchlist.recycler.WatchlistListItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchlistItemFilter @Inject constructor() {

  fun filterUpcoming(
    item: WatchlistListItem,
    isUpcoming: Boolean,
  ): Boolean {
    if (isUpcoming) {
      val nowUtcDay = nowUtcDay()
      val releasedAt = item.movie.released
      val isUpcomingDate = releasedAt != null && releasedAt.toEpochDay() > nowUtcDay.toEpochDay()
      val isUpcomingYear = releasedAt == null && item.movie.year > nowUtcDay.year
      return isUpcomingDate || isUpcomingYear
    }
    return true
  }

  fun filterByQuery(
    item: WatchlistListItem.MovieItem,
    query: String,
  ): Boolean {
    return item.movie.title.contains(query, true) ||
      item.translation?.title?.contains(query, true) == true
  }
}
