package com.michaldrabik.ui_base.trakt.exports

import com.michaldrabik.common.errors.ErrorHelper
import com.michaldrabik.common.errors.ShowlyError
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
      handleError(error)
    }
  }

  private suspend fun exportWatchlist() {
    Timber.d("Exporting watchlist...")

    val shows = localSource.watchlistShows.getAll()
      .map { SyncExportItem.create(it.idTrakt) }
      .toMutableList()

    val movies = mutableListOf<SyncExportItem>()
    if (settingsRepository.isMoviesEnabled) {
      localSource.watchlistMovies.getAll()
        .mapTo(movies) { SyncExportItem.create(it.idTrakt) }
    }

    Timber.d("Exporting ${shows.size} shows & ${movies.size} movies...")

    while (true) {
      val showsChunk = shows.take(250)
      val moviesChunk = movies.take(250)
      if (showsChunk.isEmpty() && moviesChunk.isEmpty()) {
        Timber.d("No more chunks. Breaking.")
        break
      }
      Timber.d("Exporting chunk of ${showsChunk.size} shows & ${moviesChunk.size} movies...")
      val request = SyncExportRequest(shows = showsChunk, movies = moviesChunk)
      remoteSource.trakt.postSyncWatchlist(request)

      shows.removeAll(showsChunk)
      movies.removeAll(moviesChunk)

      delay(TRAKT_LIMIT_DELAY_MS)
    }
  }

  private suspend fun handleError(error: Throwable) {
    val showlyError = ErrorHelper.parse(error)
    when {
      showlyError == ShowlyError.AccountLimitsError -> {
        Timber.w("Account limits reached for Watchlist.")
        throw error
      }
      retryCount < MAX_RETRY_COUNT -> {
        Timber.w("exportWatchlist failed. Will retry in $RETRY_DELAY_MS ms... $error")
        retryCount += 1
        delay(RETRY_DELAY_MS)
        runExport()
      }
      else -> throw error
    }
  }
}
