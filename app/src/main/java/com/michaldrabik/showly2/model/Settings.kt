package com.michaldrabik.showly2.model

data class Settings(
  val isInitialRun: Boolean,
  val pushNotificationsEnabled: Boolean,
  val episodesNotificationsEnabled: Boolean,
  val episodesNotificationsDelay: NotificationDelay,
  val myShowsRunningSortBy: SortOrder,
  val myShowsIncomingSortBy: SortOrder,
  val myShowsEndedSortBy: SortOrder,
  val myShowsRecentsAmount: Int
)