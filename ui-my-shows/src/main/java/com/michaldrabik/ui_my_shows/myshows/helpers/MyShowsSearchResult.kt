package com.michaldrabik.ui_my_shows.myshows.helpers

import com.michaldrabik.ui_my_shows.myshows.recycler.MyShowsItem

data class MyShowsSearchResult(
  val items: List<MyShowsItem>,
  val type: ResultType
)

enum class ResultType {
  EMPTY,
  RESULTS,
  NO_RESULTS
}
