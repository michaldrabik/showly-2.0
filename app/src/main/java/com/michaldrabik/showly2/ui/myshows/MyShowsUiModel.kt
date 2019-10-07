package com.michaldrabik.showly2.ui.myshows

data class MyShowsUiModel(
  val recentShows: List<MyShowsListItem>? = null,
  val runningShows: List<MyShowsListItem>? = null,
  val endedShows: List<MyShowsListItem>? = null,
  val incomingShows: List<MyShowsListItem>? = null,
  val updateListItem: MyShowsListItem? = null,
  val listPosition: Pair<Int, Int>? = null
)