package com.michaldrabik.ui_base.trakt.imports

import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_local.database.model.WatchlistMovie
import com.michaldrabik.data_local.database.model.WatchlistShow
import com.michaldrabik.data_local.utilities.TransactionsProvider
import com.michaldrabik.data_remote.RemoteDataSource
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_base.trakt.TraktSyncRunner
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraktImportWatchlistRunner @Inject constructor(
  private val remoteSource: RemoteDataSource,
  private val localSource: LocalDataSource,
  private val transactions: TransactionsProvider,
  private val mappers: Mappers,
  private val settingsRepository: SettingsRepository,
  userTraktManager: UserTraktManager
) : TraktSyncRunner(userTraktManager) {

  override suspend fun run(): Int {
    Timber.d("Initialized.")

    var syncedCount = 0
    checkAuthorization()

    resetRetries()
    syncedCount += runShows()

    resetRetries()
    syncedCount += runMovies()

    Timber.d("Finished with success.")
    return syncedCount
  }

  private suspend fun runShows(): Int = try {
    importShowsWatchlist()
  } catch (error: Throwable) {
    if (retryCount < MAX_RETRY_COUNT) {
      Timber.w("runShows HTTP failed. Will retry in $RETRY_DELAY_MS ms... $error")
      retryCount += 1
      delay(RETRY_DELAY_MS)
      runShows()
    } else {
      throw error
    }
  }

  private suspend fun runMovies(): Int {
    if (!settingsRepository.isMoviesEnabled) {
      Timber.d("Movies are disabled. Exiting...")
      return 0
    }

    return try {
      importMoviesWatchlist()
    } catch (error: Throwable) {
      if (retryCount < MAX_RETRY_COUNT) {
        Timber.w("runMovies HTTP failed. Will retry in $RETRY_DELAY_MS ms... $error")
        retryCount += 1
        delay(RETRY_DELAY_MS)
        runMovies()
      } else {
        throw error
      }
    }
  }

  private suspend fun importShowsWatchlist(): Int {
    Timber.d("Importing shows watchlist...")
    val syncResults = remoteSource.trakt.fetchSyncShowsWatchlist()
      .filter { it.show != null }
      .distinctBy { it.show!!.ids?.trakt }

    val localShowsIds =
      localSource.watchlistShows.getAllTraktIds()
        .plus(localSource.myShows.getAllTraktIds())
        .plus(localSource.archiveShows.getAllTraktIds())
        .distinct()

    syncResults
      .forEachIndexed { index, result ->
        Timber.d("Processing \'${result.show!!.title}\'...")
        val showUi = mappers.show.fromNetwork(result.show!!)
        progressListener?.invoke(showUi.title, index, syncResults.size)
        try {
          val showId = result.show!!.ids?.trakt ?: -1
          transactions.withTransaction {
            if (showId !in localShowsIds) {
              val show = mappers.show.fromNetwork(result.show!!)
              val showDb = mappers.show.toDatabase(show)
              localSource.shows.upsert(listOf(showDb))
              localSource.watchlistShows.insert(WatchlistShow.fromTraktId(showId, result.lastListedMillis()))
            }
          }
        } catch (error: Throwable) {
          Timber.w("Processing \'${result.show!!.title}\' failed. Skipping...")
          Logger.record(error, "TraktImportWatchlistRunner::importShowsWatchlist()")
        }
      }

    return syncResults.size
  }

  private suspend fun importMoviesWatchlist(): Int {
    Timber.d("Importing movies watchlist...")
    val syncResults = remoteSource.trakt.fetchSyncMoviesWatchlist()
      .filter { it.movie != null }
      .distinctBy { it.movie!!.ids?.trakt }

    val localMoviesIds =
      localSource.watchlistMovies.getAllTraktIds()
        .plus(localSource.myMovies.getAllTraktIds())
        .plus(localSource.archiveMovies.getAllTraktIds())
        .distinct()

    syncResults
      .forEachIndexed { index, result ->
        Timber.d("Processing \'${result.movie!!.title}\'...")
        val movieUi = mappers.movie.fromNetwork(result.movie!!)
        progressListener?.invoke(movieUi.title, index, syncResults.size)
        try {
          val movieId = result.movie!!.ids?.trakt ?: -1
          transactions.withTransaction {
            if (movieId !in localMoviesIds) {
              val movie = mappers.movie.fromNetwork(result.movie!!)
              val movieDb = mappers.movie.toDatabase(movie)
              localSource.movies.upsert(listOf(movieDb))
              localSource.watchlistMovies.insert(WatchlistMovie.fromTraktId(movieId, result.lastListedMillis()))
            }
          }
        } catch (error: Throwable) {
          Timber.w("Processing \'${result.movie!!.title}\' failed. Skipping...")
          Logger.record(error, "TraktImportWatchlistRunner::importMoviesWatchlist()")
        }
      }

    return syncResults.size
  }
}
