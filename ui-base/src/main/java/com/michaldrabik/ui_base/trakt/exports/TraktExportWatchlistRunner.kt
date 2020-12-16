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
    runExport(authToken)

    isRunning = false
    Timber.d("Finished with success.")
    return 0
  }

  private suspend fun runExport(authToken: TraktAuthToken) {
    try {
      exportWatchlist(authToken)
    } catch (error: Throwable) {
      if (retryCount < MAX_RETRY_COUNT) {
        Timber.w("exportWatchlist failed. Will retry in $RETRY_DELAY_MS ms... $error")
        retryCount += 1
        delay(RETRY_DELAY_MS)
        runExport(authToken)
      } else {
        isRunning = false
        throw error
      }
    }
  }

  private suspend fun exportWatchlist(token: TraktAuthToken) {
    Timber.d("Exporting watchlist...")

    val shows = database.watchlistShowsDao().getAll()
      .map { SyncExportItem.create(it.idTrakt) }

    val movies = mutableListOf<SyncExportItem>()
    if (settingsRepository.isMoviesEnabled()) {
      database.watchlistMoviesDao().getAll()
        .mapTo(movies) { SyncExportItem.create(it.idTrakt) }
    }

    Timber.d("Exporting ${shows.size} shows & ${movies.size} movies...")

    val request = SyncExportRequest(shows = shows, movies = movies)
    cloud.traktApi.postSyncWatchlist(token.token, request)
  }
}
