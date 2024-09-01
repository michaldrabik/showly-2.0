package com.michaldrabik.data_local.database.dao.helpers

import com.michaldrabik.data_local.database.model.Episode
import com.michaldrabik.data_local.database.model.Season
import com.michaldrabik.data_local.database.model.Settings
import com.michaldrabik.data_local.database.model.Show

object TestData {

  fun createShow() =
    Show(
      idTrakt = 1,
      idTvdb = 1,
      idTmdb = 1,
      idImdb = "idImdb",
      idSlug = "idSlug",
      idTvrage = 1,
      title = "Title",
      year = 2000,
      overview = "Overview",
      firstAired = "FirstAired",
      runtime = 60,
      airtimeDay = "AirtimeDay",
      airtimeTime = "AirtimeTime",
      airtimeTimezone = "AirtimeTimezone",
      certification = "Certification",
      network = "Network",
      country = "Country",
      trailer = "Trailer",
      homepage = "Homepage",
      status = "Status",
      rating = 0F,
      votes = 0L,
      commentCount = 0L,
      genres = "Genres",
      airedEpisodes = 0,
      createdAt = 0,
      updatedAt = 0,
    )

  fun createSettings() =
    Settings(
      id = 0,
      isInitialRun = false,
      pushNotificationsEnabled = false,
      episodesNotificationsEnabled = false,
      episodesNotificationsDelay = 0,
      myShowsRecentsAmount = 0,
      myShowsRunningSortBy = "",
      myShowsIncomingSortBy = "",
      myShowsEndedSortBy = "",
      myShowsAllSortBy = "",
      myShowsRunningIsCollapsed = false,
      myShowsIncomingIsCollapsed = false,
      myShowsEndedIsCollapsed = false,
      myShowsRunningIsEnabled = false,
      myShowsIncomingIsEnabled = false,
      myShowsEndedIsEnabled = false,
      myShowsRecentIsEnabled = false,
      seeLaterShowsSortBy = "",
      showAnticipatedShows = false,
      discoverFilterGenres = "",
      discoverFilterNetworks = "",
      discoverFilterFeed = "",
      traktSyncSchedule = "",
      traktQuickSyncEnabled = false,
      traktQuickRemoveEnabled = false,
      watchlistSortBy = "",
      archiveShowsSortBy = "",
      archiveShowsIncludeStatistics = false,
      specialSeasonsEnabled = false,
      showAnticipatedMovies = false,
      discoverMoviesFilterGenres = "",
      discoverMoviesFilterFeed = "",
      myMoviesAllSortBy = "",
      seeLaterMoviesSortBy = "",
      progressMoviesSortBy = "",
      showCollectionShows = false,
      showCollectionMovies = false,
      widgetsShowLabel = false,
      myMoviesRecentIsEnabled = false,
      quickRateEnabled = false,
      listsSortBy = "",
      progressUpcomingEnabled = false,
    )

  fun createEpisode() =
    Episode(
      idTrakt = 1,
      idSeason = 1,
      idShowTrakt = 1,
      idShowTvdb = 1,
      idShowImdb = "",
      idShowTmdb = 1,
      seasonNumber = 1,
      episodeNumber = 1,
      episodeNumberAbs = 1,
      episodeOverview = "",
      title = "",
      firstAired = null,
      commentsCount = 0,
      rating = 0F,
      runtime = 60,
      votesCount = 0,
      isWatched = false,
      lastExportedAt = null,
      lastWatchedAt = null,
    )

  fun createSeason() =
    Season(
      idTrakt = 1,
      idShowTrakt = 1,
      seasonNumber = 1,
      seasonTitle = "",
      seasonOverview = "",
      seasonFirstAired = null,
      episodesCount = 0,
      episodesAiredCount = 0,
      rating = 0f,
      isWatched = false,
    )
}
