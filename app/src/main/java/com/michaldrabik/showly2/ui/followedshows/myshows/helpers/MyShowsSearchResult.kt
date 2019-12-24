package com.michaldrabik.showly2.ui.followedshows.myshows.helpers

import com.michaldrabik.showly2.ui.followedshows.myshows.recycler.MyShowsListItem

data class MyShowsSearchResult(
  val items: List<MyShowsListItem>,
  val type: ResultType
)

enum class ResultType {
  EMPTY,
  RESULTS,
  NO_RESULTS
}
