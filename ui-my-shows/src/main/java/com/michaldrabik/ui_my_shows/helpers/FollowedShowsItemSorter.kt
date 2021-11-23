package com.michaldrabik.ui_my_shows.helpers

import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortOrder.DATE_ADDED
import com.michaldrabik.ui_model.SortOrder.NAME
import com.michaldrabik.ui_model.SortOrder.NEWEST
import com.michaldrabik.ui_model.SortOrder.RATING
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_model.SortType.ASCENDING
import com.michaldrabik.ui_model.SortType.DESCENDING
import com.michaldrabik.ui_model.Translation
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FollowedShowsItemSorter @Inject constructor() {

  fun sort(sortOrder: SortOrder, sortType: SortType) = when (sortType) {
    ASCENDING -> sortAscending(sortOrder)
    DESCENDING -> sortDescending(sortOrder)
  }

  private fun sortAscending(sortOrder: SortOrder) = when (sortOrder) {
    NAME -> compareBy { getTitle(it) }
    RATING -> compareBy { it.first.rating }
    DATE_ADDED -> compareBy { it.first.createdAt }
    NEWEST -> compareBy<Pair<Show, Translation?>> { it.first.firstAired }.thenBy { it.first.year }
    else -> throw IllegalStateException("Invalid sort order")
  }

  private fun sortDescending(sortOrder: SortOrder) = when (sortOrder) {
    NAME -> compareByDescending { getTitle(it) }
    RATING -> compareByDescending { it.first.rating }
    DATE_ADDED -> compareByDescending { it.first.createdAt }
    NEWEST -> compareByDescending<Pair<Show, Translation?>> { it.first.firstAired }.thenByDescending { it.first.year }
    else -> throw IllegalStateException("Invalid sort order")
  }

  private fun getTitle(item: Pair<Show, Translation?>): String {
    val translatedTitle =
      if (item.second?.hasTitle == true) item.second?.title
      else item.first.titleNoThe
    return translatedTitle?.uppercase() ?: ""
  }
}
