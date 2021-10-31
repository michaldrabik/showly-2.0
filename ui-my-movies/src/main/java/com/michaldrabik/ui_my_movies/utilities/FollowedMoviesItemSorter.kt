package com.michaldrabik.ui_my_movies.utilities

import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortOrder.DATE_ADDED
import com.michaldrabik.ui_model.SortOrder.NAME
import com.michaldrabik.ui_model.SortOrder.NEWEST
import com.michaldrabik.ui_model.SortOrder.RATING
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_model.SortType.ASCENDING
import com.michaldrabik.ui_model.SortType.DESCENDING
import com.michaldrabik.ui_model.Translation
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FollowedMoviesItemSorter @Inject constructor() {

  fun sort(sortOrder: SortOrder, sortType: SortType) = when (sortType) {
    ASCENDING -> sortAscending(sortOrder)
    DESCENDING -> sortDescending(sortOrder)
  }

  private fun sortAscending(sortOrder: SortOrder) = when (sortOrder) {
    NAME -> compareBy { getTitle(it) }
    RATING -> compareBy { it.first.rating }
    DATE_ADDED -> compareBy { it.first.createdAt }
    NEWEST -> compareBy<Pair<Movie, Translation?>> { it.first.released }.thenBy { it.first.year }
    else -> throw IllegalStateException("Invalid sort order")
  }

  private fun sortDescending(sortOrder: SortOrder) = when (sortOrder) {
    NAME -> compareByDescending { getTitle(it) }
    RATING -> compareByDescending { it.first.rating }
    DATE_ADDED -> compareByDescending { it.first.createdAt }
    NEWEST -> compareByDescending<Pair<Movie, Translation?>> { it.first.released }.thenByDescending { it.first.year }
    else -> throw IllegalStateException("Invalid sort order")
  }

  private fun getTitle(item: Pair<Movie, Translation?>): String {
    val translatedTitle =
      if (item.second?.hasTitle == false) null
      else item.second?.title
    return (translatedTitle ?: item.first.titleNoThe).uppercase(Locale.ROOT)
  }
}
