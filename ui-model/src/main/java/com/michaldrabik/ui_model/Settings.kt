package com.michaldrabik.ui_model

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
  val watchlistSortOrder: SortOrder,
  val archiveShowsIncludeStatistics: Boolean,
  val specialSeasonsEnabled: Boolean
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
      myShowsRecentsAmount = 4,
      seeLaterShowsSortBy = SortOrder.NAME,
      archiveShowsSortBy = SortOrder.NAME,
      showAnticipatedShows = false,
      discoverFilterFeed = DiscoverSortOrder.HOT,
      discoverFilterGenres = emptyList(),
      traktSyncSchedule = TraktSyncSchedule.OFF,
      traktQuickSyncEnabled = false,
      traktQuickRemoveEnabled = false,
      watchlistSortOrder = SortOrder.NAME,
      archiveShowsIncludeStatistics = true,
      specialSeasonsEnabled = false
    )
  }
}
