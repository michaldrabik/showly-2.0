package com.michaldrabik.showly2.model

import com.michaldrabik.showly2.Config

data class Settings(
  val isInitialRun: Boolean,
  val pushNotificationsEnabled: Boolean,
  val episodesNotificationsEnabled: Boolean,
  val episodesNotificationsDelay: NotificationDelay,
  val seeLaterShowsSortBy: SortOrder,
  val archiveShowsSortBy: SortOrder,
  val myShowsWatchingSortBy: SortOrder,
  val myShowsUpcomingSortBy: SortOrder,
  val myShowsFinishedSortBy: SortOrder,
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
  val traktSyncSchedule: TraktSyncSchedule,
  val traktQuickSyncEnabled: Boolean,
  val traktQuickRemoveEnabled: Boolean,
  val watchlistSortOrder: SortOrder
) {

  companion object {
    fun createInitial() = Settings(
      isInitialRun = true,
      pushNotificationsEnabled = true,
      episodesNotificationsEnabled = true,
      episodesNotificationsDelay = NotificationDelay.WHEN_AVAILABLE,
      myShowsFinishedSortBy = SortOrder.NAME,
      myShowsUpcomingSortBy = SortOrder.NAME,
      myShowsWatchingSortBy = SortOrder.NAME,
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
      archiveShowsSortBy = SortOrder.NAME,
      showAnticipatedShows = true,
      discoverFilterFeed = DiscoverSortOrder.HOT,
      discoverFilterGenres = emptyList(),
      traktSyncSchedule = TraktSyncSchedule.OFF,
      traktQuickSyncEnabled = false,
      traktQuickRemoveEnabled = false,
      watchlistSortOrder = SortOrder.NAME
    )
  }
}
