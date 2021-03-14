package com.michaldrabik.ui_model

enum class SortOrder(
  val displayString: Int
) {
  NAME(R.string.textSortName),
  NEWEST(R.string.textSortNewest),
  RATING(R.string.textSortRated),
  DATE_ADDED(R.string.textSortDateAdded),
  DATE_UPDATED(R.string.textSortDateUpdated),
  RECENTLY_WATCHED(R.string.textSortRecentlyWatched),
  EPISODES_LEFT(R.string.textSortEpisodesLeft)
}
