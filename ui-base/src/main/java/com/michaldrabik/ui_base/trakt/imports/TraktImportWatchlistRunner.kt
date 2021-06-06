package com.michaldrabik.ui_base.trakt.imports

import androidx.room.withTransaction
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_local.database.model.WatchlistMovie
import com.michaldrabik.data_local.database.model.WatchlistShow
import com.michaldrabik.data_remote.Cloud
import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.repository.TraktAuthToken
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_base.trakt.TraktSyncRunner
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
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

    var syncedCount = 0
    val authToken = checkAuthorization()

    resetRetries()
    syncedCount += runShows(authToken)

    resetRetries()
    syncedCount += runMovies(authToken)

    isRunning = false
    Timber.d("Finished with success.")
    return syncedCount
  }

  private suspend fun runShows(authToken: TraktAuthToken): Int = try {
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

  private suspend fun runMovies(authToken: TraktAuthToken): Int {
    if (!settingsRepository.isMoviesEnabled) {
      Timber.d("Movies are disabled. Exiting...")
      return 0
    }

    return try {
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

  private suspend fun importShowsWatchlist(token: TraktAuthToken): Int {
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

    return syncResults.size
  }

  private suspend fun importMoviesWatchlist(token: TraktAuthToken): Int {
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

    return syncResults.size
  }
}
