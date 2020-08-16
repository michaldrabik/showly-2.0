package com.michaldrabik.showly2.model.mappers

import com.michaldrabik.showly2.model.Genre
import com.michaldrabik.showly2.model.NotificationDelay
import com.michaldrabik.showly2.model.Settings
import javax.inject.Inject
import com.michaldrabik.storage.database.model.Settings as SettingsDb

class SettingsMapper @Inject constructor() {

  fun fromDatabase(settings: SettingsDb) = Settings(
    isInitialRun = settings.isInitialRun,
    pushNotificationsEnabled = settings.pushNotificationsEnabled,
    episodesNotificationsEnabled = settings.episodesNotificationsEnabled,
    episodesNotificationsDelay = NotificationDelay.fromDelay(settings.episodesNotificationsDelay),
    myShowsWatchingSortBy = enumValueOf(settings.myShowsRunningSortBy),
    myShowsUpcomingSortBy = enumValueOf(settings.myShowsIncomingSortBy),
    myShowsFinishedSortBy = enumValueOf(settings.myShowsEndedSortBy),
    myShowsAllSortBy = enumValueOf(settings.myShowsAllSortBy),
    myShowsRunningIsCollapsed = settings.myShowsRunningIsCollapsed,
    myShowsIncomingIsCollapsed = settings.myShowsIncomingIsCollapsed,
    myShowsEndedIsCollapsed = settings.myShowsEndedIsCollapsed,
    myShowsRunningIsEnabled = settings.myShowsRunningIsEnabled,
    myShowsIncomingIsEnabled = settings.myShowsIncomingIsEnabled,
    myShowsEndedIsEnabled = settings.myShowsEndedIsEnabled,
    myShowsRecentIsEnabled = settings.myShowsRecentIsEnabled,
    myShowsRecentsAmount = settings.myShowsRecentsAmount,
    seeLaterShowsSortBy = enumValueOf(settings.seeLaterShowsSortBy),
    showAnticipatedShows = settings.showAnticipatedShows,
    discoverFilterFeed = enumValueOf(settings.discoverFilterFeed),
    discoverFilterGenres = settings.discoverFilterGenres.split(",").filter { it.isNotBlank() }.map { Genre.valueOf(it) },
    traktSyncSchedule = enumValueOf(settings.traktSyncSchedule),
    traktQuickSyncEnabled = settings.traktQuickSyncEnabled,
    watchlistSortOrder = enumValueOf(settings.watchlistSortBy)
  )

  fun toDatabase(settings: Settings) = SettingsDb(
    isInitialRun = settings.isInitialRun,
    pushNotificationsEnabled = settings.pushNotificationsEnabled,
    episodesNotificationsEnabled = settings.episodesNotificationsEnabled,
    episodesNotificationsDelay = settings.episodesNotificationsDelay.delayMs,
    myShowsRunningSortBy = settings.myShowsWatchingSortBy.name,
    myShowsIncomingSortBy = settings.myShowsUpcomingSortBy.name,
    myShowsEndedSortBy = settings.myShowsFinishedSortBy.name,
    myShowsAllSortBy = settings.myShowsAllSortBy.name,
    myShowsRunningIsCollapsed = settings.myShowsRunningIsCollapsed,
    myShowsIncomingIsCollapsed = settings.myShowsIncomingIsCollapsed,
    myShowsEndedIsCollapsed = settings.myShowsEndedIsCollapsed,
    myShowsRunningIsEnabled = settings.myShowsRunningIsEnabled,
    myShowsIncomingIsEnabled = settings.myShowsIncomingIsEnabled,
    myShowsEndedIsEnabled = settings.myShowsEndedIsEnabled,
    myShowsRecentIsEnabled = settings.myShowsRecentIsEnabled,
    myShowsRecentsAmount = settings.myShowsRecentsAmount,
    seeLaterShowsSortBy = settings.seeLaterShowsSortBy.name,
    showAnticipatedShows = settings.showAnticipatedShows,
    discoverFilterFeed = settings.discoverFilterFeed.name,
    discoverFilterGenres = settings.discoverFilterGenres.joinToString(",") { it.name },
    traktSyncSchedule = settings.traktSyncSchedule.name,
    traktQuickSyncEnabled = settings.traktQuickSyncEnabled,
    watchlistSortBy = settings.watchlistSortOrder.name
  )
}
