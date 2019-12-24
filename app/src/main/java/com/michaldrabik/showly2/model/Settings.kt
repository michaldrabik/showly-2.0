package com.michaldrabik.showly2.model

import com.michaldrabik.showly2.Config

data class Settings(
  val isInitialRun: Boolean,
  val pushNotificationsEnabled: Boolean,
  val episodesNotificationsEnabled: Boolean,
  val episodesNotificationsDelay: NotificationDelay,
  val seeLaterShowsSortBy: SortOrder,
  val myShowsRunningSortBy: SortOrder,
  val myShowsIncomingSortBy: SortOrder,
  val myShowsEndedSortBy: SortOrder,
  val myShowsRecentsAmount: Int
) {

  companion object {
    fun createInitial() = Settings(
      isInitialRun = true,
      pushNotificationsEnabled = true,
      episodesNotificationsEnabled = true,
      episodesNotificationsDelay = NotificationDelay.WHEN_AVAILABLE,
      myShowsEndedSortBy = SortOrder.NAME,
      myShowsIncomingSortBy = SortOrder.NAME,
      myShowsRunningSortBy = SortOrder.NAME,
      myShowsRecentsAmount = Config.MY_SHOWS_RECENTS_DEFAULT,
      seeLaterShowsSortBy = SortOrder.NAME
    )
  }
}
