package com.michaldrabik.ui_base.trakt.imports

import androidx.room.withTransaction
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.network.Cloud
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.WatchlistMovie
import com.michaldrabik.storage.database.model.WatchlistShow
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_base.trakt.TraktSyncRunner
import com.michaldrabik.ui_repository.SettingsRepository
import com.michaldrabik.ui_repository.TraktAuthToken
import com.michaldrabik.ui_repository.UserTraktManager
import com.michaldrabik.ui_repository.mappers.Mappers
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject

@AppScope
class TraktImportWatchlistRunner @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val mappers: Mappers,
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
    runMovies(authToken)

    isRunning = false
    Timber.d("Finished with success.")
    return 0
  }

  private suspend fun runShows(authToken: TraktAuthToken) {
    try {
      importShowsWatchlist(authToken)
    } catch (error: Throwable) {
      if (retryCount < MAX_RETRY_COUNT) {
        Timber.w("runShows HTTP failed. Will retry in $RETRY_DELAY_MS ms... $error")
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
    if (!settingsRepository.isMoviesEnabled) {
      Timber.d("Movies are disabled. Exiting...")
      return
    }

    try {
      importMoviesWatchlist(authToken)
    } catch (error: Throwable) {
      if (retryCount < MAX_RETRY_COUNT) {
        Timber.w("runMovies HTTP failed. Will retry in $RETRY_DELAY_MS ms... $error")
        retryCount += 1
        delay(RETRY_DELAY_MS)
        runMovies(authToken)
      } else {
        isRunning = false
        throw error
      }
    }
  }

  private suspend fun importShowsWatchlist(token: TraktAuthToken) {
    Timber.d("Importing shows watchlist...")
    val syncResults = cloud.traktApi.fetchSyncShowsWatchlist(token.token)
      .filter { it.show != null }
      .distinctBy { it.show!!.ids?.trakt }

    val localShowsIds =
      database.watchlistShowsDao().getAllTraktIds()
        .plus(database.myShowsDao().getAllTraktIds())
        .plus(database.archiveShowsDao().getAllTraktIds())
        .distinct()

    syncResults
      .forEachIndexed { index, result ->
        Timber.d("Processing \'${result.show!!.title}\'...")
        val showUi = mappers.show.fromNetwork(result.show!!)
        progressListener?.invoke(showUi.title, index, syncResults.size)
        try {
          val showId = result.show!!.ids?.trakt ?: -1
          database.withTransaction {
            if (showId !in localShowsIds) {
              val show = mappers.show.fromNetwork(result.show!!)
              val showDb = mappers.show.toDatabase(show)
              database.showsDao().upsert(listOf(showDb))
              database.watchlistShowsDao().insert(WatchlistShow.fromTraktId(showId, result.lastListedMillis()))
            }
          }
        } catch (error: Throwable) {
          Timber.w("Processing \'${result.show!!.title}\' failed. Skipping...")
          Logger.record(error, "Source" to "Import Shows Watchlist")
        }
      }
  }

  private suspend fun importMoviesWatchlist(token: TraktAuthToken) {
    Timber.d("Importing movies watchlist...")
    val syncResults = cloud.traktApi.fetchSyncMoviesWatchlist(token.token)
      .filter { it.movie != null }
      .distinctBy { it.movie!!.ids?.trakt }

    val localMoviesIds =
      database.watchlistMoviesDao().getAllTraktIds()
        .plus(database.myMoviesDao().getAllTraktIds())
        .distinct()

    syncResults
      .forEachIndexed { index, result ->
        Timber.d("Processing \'${result.movie!!.title}\'...")
        val movieUi = mappers.movie.fromNetwork(result.movie!!)
        progressListener?.invoke(movieUi.title, index, syncResults.size)
        try {
          val movieId = result.movie!!.ids?.trakt ?: -1
          database.withTransaction {
            if (movieId !in localMoviesIds) {
              val movie = mappers.movie.fromNetwork(result.movie!!)
              val movieDb = mappers.movie.toDatabase(movie)
              database.moviesDao().upsert(listOf(movieDb))
              database.watchlistMoviesDao().insert(WatchlistMovie.fromTraktId(movieId, result.lastListedMillis()))
            }
          }
        } catch (error: Throwable) {
          Timber.w("Processing \'${result.movie!!.title}\' failed. Skipping...")
          Logger.record(error, "Source" to "Import Movies Watchlist")
        }
      }
  }
}
