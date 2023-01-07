package com.michaldrabik.ui_my_movies.common.helpers

import com.michaldrabik.common.extensions.nowUtcDay
import com.michaldrabik.ui_my_movies.common.recycler.CollectionListItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CollectionItemFilter @Inject constructor() {

  fun filterUpcoming(
    item: CollectionListItem,
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
    item: CollectionListItem.MovieItem,
    query: String,
  ): Boolean {
    return item.movie.title.contains(query, true) ||
      item.translation?.title?.contains(query, true) == true
  }
}
