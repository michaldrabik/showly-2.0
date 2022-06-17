package com.michaldrabik.repository.mappers

import com.michaldrabik.ui_model.Genre
import com.michaldrabik.ui_model.Network
import com.michaldrabik.ui_model.NotificationDelay
import com.michaldrabik.ui_model.Settings
import javax.inject.Inject
import com.michaldrabik.data_local.database.model.Settings as SettingsDb

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
    myMoviesRecentIsEnabled = settings.myMoviesRecentIsEnabled,
    myRecentsAmount = settings.myShowsRecentsAmount,
    watchlistShowsSortBy = enumValueOf(settings.seeLaterShowsSortBy),
    archiveShowsSortBy = enumValueOf(settings.archiveShowsSortBy),
    showAnticipatedShows = settings.showAnticipatedShows,
    discoverFilterFeed = enumValueOf(settings.discoverFilterFeed),
    discoverFilterGenres = settings.discoverFilterGenres.split(",").filter { it.isNotBlank() }.map { Genre.valueOf(it) },
    discoverFilterNetworks = settings.discoverFilterNetworks.split(",").filter { it.isNotBlank() }.map { Network.valueOf(it) },
    traktSyncSchedule = enumValueOf(settings.traktSyncSchedule),
    traktQuickSyncEnabled = settings.traktQuickSyncEnabled,
    traktQuickRemoveEnabled = settings.traktQuickRemoveEnabled,
    progressSortOrder = enumValueOf(settings.watchlistSortBy),
    archiveIncludeStatistics = settings.archiveShowsIncludeStatistics,
    specialSeasonsEnabled = settings.specialSeasonsEnabled,
    showAnticipatedMovies = settings.showAnticipatedMovies,
    discoverMoviesFilterGenres = settings.discoverMoviesFilterGenres.split(",").filter { it.isNotBlank() }.map { Genre.valueOf(it) },
    discoverMoviesFilterFeed = enumValueOf(settings.discoverMoviesFilterFeed),
    myMoviesAllSortBy = enumValueOf(settings.myMoviesAllSortBy),
    watchlistMoviesSortBy = enumValueOf(settings.seeLaterMoviesSortBy),
    progressMoviesSortBy = enumValueOf(settings.progressMoviesSortBy),
    showCollectionShows = settings.showCollectionShows,
    showCollectionMovies = settings.showCollectionMovies,
    widgetsShowLabel = settings.widgetsShowLabel,
    traktQuickRateEnabled = settings.quickRateEnabled,
    listsSortBy = enumValueOf(settings.listsSortBy),
    progressUpcomingEnabled = settings.progressUpcomingEnabled
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
    myMoviesRecentIsEnabled = settings.myMoviesRecentIsEnabled,
    myShowsRecentsAmount = settings.myRecentsAmount,
    seeLaterShowsSortBy = settings.watchlistShowsSortBy.name,
    archiveShowsSortBy = settings.archiveShowsSortBy.name,
    showAnticipatedShows = settings.showAnticipatedShows,
    discoverFilterFeed = settings.discoverFilterFeed.name,
    discoverFilterGenres = settings.discoverFilterGenres.joinToString(",") { it.name },
    discoverFilterNetworks = settings.discoverFilterNetworks.joinToString(",") { it.name },
    traktSyncSchedule = settings.traktSyncSchedule.name,
    traktQuickSyncEnabled = settings.traktQuickSyncEnabled,
    traktQuickRemoveEnabled = settings.traktQuickRemoveEnabled,
    watchlistSortBy = settings.progressSortOrder.name,
    archiveShowsIncludeStatistics = settings.archiveIncludeStatistics,
    specialSeasonsEnabled = settings.specialSeasonsEnabled,
    showAnticipatedMovies = settings.showAnticipatedMovies,
    discoverMoviesFilterFeed = settings.discoverMoviesFilterFeed.name,
    discoverMoviesFilterGenres = settings.discoverMoviesFilterGenres.joinToString(",") { it.name },
    myMoviesAllSortBy = settings.myMoviesAllSortBy.name,
    seeLaterMoviesSortBy = settings.watchlistMoviesSortBy.name,
    progressMoviesSortBy = settings.progressMoviesSortBy.name,
    showCollectionShows = settings.showCollectionShows,
    showCollectionMovies = settings.showCollectionMovies,
    widgetsShowLabel = settings.widgetsShowLabel,
    quickRateEnabled = settings.traktQuickRateEnabled,
    listsSortBy = settings.listsSortBy.name,
    progressUpcomingEnabled = settings.progressUpcomingEnabled
  )
}
