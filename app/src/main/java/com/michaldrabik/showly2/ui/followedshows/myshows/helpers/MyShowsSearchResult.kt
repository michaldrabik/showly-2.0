package com.michaldrabik.showly2.ui.followedshows.myshows.helpers

import com.michaldrabik.showly2.ui.followedshows.myshows.recycler.MyShowsItem

data class MyShowsSearchResult(
  val items: List<MyShowsItem>,
  val type: ResultType
)

enum class ResultType {
  EMPTY,
  RESULTS,
  NO_RESULTS
}
