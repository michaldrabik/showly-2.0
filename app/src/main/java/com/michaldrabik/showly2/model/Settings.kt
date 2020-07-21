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
  val myShowsAllSortBy: SortOrder,
  val myShowsRunningIsCollapsed: Boolean,
  val myShowsIncomingIsCollapsed: Boolean,
  val myShowsEndedIsCollapsed: Boolean,
  val myShowsRunningIsEnabled: Boolean,
  val myShowsIncomingIsEnabled: Boolean,
  val myShowsEndedIsEnabled: Boolean,
  val myShowsRecentIsEnabled: Boolean,
  val myShowsRecentsAmount: Int,
  val showAnticipatedShows: Boolean,
  val discoverFilterGenres: List<Genre>,
  val discoverFilterFeed: DiscoverSortOrder,
  val traktSyncSchedule: TraktSyncSchedule
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
      myShowsAllSortBy = SortOrder.NAME,
      myShowsEndedIsCollapsed = true,
      myShowsIncomingIsCollapsed = true,
      myShowsRunningIsCollapsed = true,
      myShowsEndedIsEnabled = true,
      myShowsIncomingIsEnabled = true,
      myShowsRunningIsEnabled = true,
      myShowsRecentIsEnabled = true,
      myShowsRecentsAmount = Config.MY_SHOWS_RECENTS_DEFAULT,
      seeLaterShowsSortBy = SortOrder.NAME,
      showAnticipatedShows = true,
      discoverFilterFeed = DiscoverSortOrder.HOT,
      discoverFilterGenres = emptyList(),
      traktSyncSchedule = TraktSyncSchedule.OFF
    )
  }
}
