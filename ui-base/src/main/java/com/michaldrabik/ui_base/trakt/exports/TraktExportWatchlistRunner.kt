package com.michaldrabik.ui_base.trakt.exports

import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_remote.RemoteDataSource
import com.michaldrabik.data_remote.trakt.model.SyncExportItem
import com.michaldrabik.data_remote.trakt.model.SyncExportRequest
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.trakt.TraktSyncRunner
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraktExportWatchlistRunner @Inject constructor(
  private val remoteSource: RemoteDataSource,
  private val localSource: LocalDataSource,
  private val settingsRepository: SettingsRepository,
  userTraktManager: UserTraktManager
) : TraktSyncRunner(userTraktManager) {

  override suspend fun run(): Int {
    Timber.d("Initialized.")

    checkAuthorization()
    runExport()

    Timber.d("Finished with success.")
    return 0
  }

  private suspend fun runExport() {
    try {
      exportWatchlist()
    } catch (error: Throwable) {
      if (retryCount < MAX_RETRY_COUNT) {
        Timber.w("exportWatchlist failed. Will retry in $RETRY_DELAY_MS ms... $error")
        retryCount += 1
        delay(RETRY_DELAY_MS)
        runExport()
      } else {
        throw error
      }
    }
  }

  private suspend fun exportWatchlist() {
    Timber.d("Exporting watchlist...")

    val shows = localSource.watchlistShows.getAll()
      .map { SyncExportItem.create(it.idTrakt) }

    val movies = mutableListOf<SyncExportItem>()
    if (settingsRepository.isMoviesEnabled) {
      localSource.watchlistMovies.getAll()
        .mapTo(movies) { SyncExportItem.create(it.idTrakt) }
    }

    Timber.d("Exporting ${shows.size} shows & ${movies.size} movies...")

    val request = SyncExportRequest(shows = shows, movies = movies)
    remoteSource.trakt.postSyncWatchlist(request)
  }
}
