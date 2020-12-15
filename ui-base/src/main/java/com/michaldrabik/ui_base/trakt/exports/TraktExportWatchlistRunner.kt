package com.michaldrabik.ui_base.trakt.exports

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.network.Cloud
import com.michaldrabik.network.trakt.model.SyncExportItem
import com.michaldrabik.network.trakt.model.SyncExportRequest
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.ui_base.trakt.TraktSyncRunner
import com.michaldrabik.ui_repository.SettingsRepository
import com.michaldrabik.ui_repository.TraktAuthToken
import com.michaldrabik.ui_repository.UserTraktManager
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject

@AppScope
class TraktExportWatchlistRunner @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val settingsRepository: SettingsRepository,
  userTraktManager: UserTraktManager
) : TraktSyncRunner(userTraktManager) {

  override suspend fun run(): Int {
    Timber.d("Initialized.")
    isRunning = true

    val authToken = checkAuthorization()

    resetRetries()
    runShows(authToken)

    resetRetries()
    delay(1000)
    runMovies(authToken)

    isRunning = false
    Timber.d("Finished with success.")
    return 0
  }

  private suspend fun runShows(authToken: TraktAuthToken) {
    try {
      exportShowsWatchlist(authToken)
    } catch (error: Throwable) {
      if (retryCount < MAX_RETRY_COUNT) {
        Timber.w("exportShowsWatchlist failed. Will retry in $RETRY_DELAY_MS ms... $error")
        retryCount += 1
        delay(RETRY_DELAY_MS)
        runShows(authToken)
      } else {
        isRunning = false
        throw error
      }
    }
  }

  private suspend fun runMovies(authToken: TraktAuthToken) {
    if (!settingsRepository.isMoviesEnabled()) {
      Timber.d("Movies are disabled. Exiting...")
      return
    }
    try {
      exportMoviesWatchlist(authToken)
    } catch (error: Throwable) {
      if (retryCount < MAX_RETRY_COUNT) {
        Timber.w("exportMoviesWatchlist failed. Will retry in $RETRY_DELAY_MS ms... $error")
        retryCount += 1
        delay(RETRY_DELAY_MS)
        runMovies(authToken)
      } else {
        isRunning = false
        throw error
      }
    }
  }

  private suspend fun exportShowsWatchlist(token: TraktAuthToken) {
    Timber.d("Exporting shows watchlist...")

    val localShows = database.watchlistShowsDao().getAll()
      .map { SyncExportItem.create(it.idTrakt) }

    val request = SyncExportRequest(shows = localShows)
    cloud.traktApi.postSyncWatchlist(token.token, request)
  }

  private suspend fun exportMoviesWatchlist(token: TraktAuthToken) {
    Timber.d("Exporting movies watchlist...")

    val movies = database.watchlistMoviesDao().getAll()
      .map { SyncExportItem.create(it.idTrakt) }

    val request = SyncExportRequest(movies = movies)
    cloud.traktApi.postSyncWatchlist(token.token, request)
  }
}
