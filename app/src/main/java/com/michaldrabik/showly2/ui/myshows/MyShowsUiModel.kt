package com.michaldrabik.showly2.ui.myshows

data class MyShowsUiModel(
  val recentShows: List<MyShowListItem>? = null,
  val runningShows: List<MyShowListItem>? = null,
  val endedShows: List<MyShowListItem>? = null,
  val incomingShows: List<MyShowListItem>? = null,
  val updateListItem: MyShowListItem? = null
)