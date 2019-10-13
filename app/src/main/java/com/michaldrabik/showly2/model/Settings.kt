package com.michaldrabik.showly2.model

data class Settings(
  val isInitialRun: Boolean,
  val myShowsRunningSortBy: SortOrder,
  val myShowsIncomingSortBy: SortOrder,
  val myShowsEndedSortBy: SortOrder
)