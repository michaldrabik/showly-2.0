package com.michaldrabik.ui_model

enum class SortOrderList(
  val slug: String,
  val displayString: Int
) {
  RANK("rank", R.string.textSortRank),
  TITLE("title", R.string.textSortName),
  NEWEST("released", R.string.textSortNewest),
  RATING("percentage", R.string.textSortRated),
  DATE_ADDED("added", R.string.textSortDateAdded);

  companion object {
    fun fromSlug(slug: String) = SortOrderList.values().firstOrNull { it.slug == slug }
  }
}
