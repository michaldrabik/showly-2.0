package com.michaldrabik.ui_base.trakt.exports

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.common.extensions.dateIsoStringFromMillis
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.network.Cloud
import com.michaldrabik.network.trakt.model.SyncExportItem
import com.michaldrabik.network.trakt.model.SyncExportRequest
import com.michaldrabik.network.trakt.model.SyncItem
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.Episode
import com.michaldrabik.storage.database.model.Movie
import com.michaldrabik.ui_base.trakt.TraktSyncRunner
import com.michaldrabik.ui_repository.SettingsRepository
import com.michaldrabik.ui_repository.TraktAuthToken
import com.michaldrabik.ui_repository.UserTraktManager
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject

@AppScope
class TraktExportWatchedRunner @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val settingsRepository: SettingsRepository,
  userTraktManager: UserTraktManager
) : TraktSyncRunner(userTraktManager) {

  override suspend fun run(): Int {
    Timber.d("Initialized.")
    isRunning = true

    Timber.d("Checking authorization...")
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
      delay(1500)
      exportShowsWatched(authToken)
    } catch (error: Throwable) {
      if (retryCount < MAX_RETRY_COUNT) {
        Timber.w("exportShowsWatched failed. Will retry in $RETRY_DELAY_MS ms... $error")
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
    }
    try {
      delay(1500)
      exportMoviesWatched(authToken)
    } catch (error: Throwable) {
      if (retryCount < MAX_RETRY_COUNT) {
        Timber.w("exportMoviesWatched failed. Will retry in $RETRY_DELAY_MS ms... $error")
        retryCount += 1
        delay(RETRY_DELAY_MS)
        runMovies(authToken)
      } else {
        isRunning = false
        throw error
      }
    }
  }

  private suspend fun exportShowsWatched(token: TraktAuthToken) {
    Timber.d("Exporting watched shows...")

    val remoteWatched = cloud.traktApi.fetchSyncWatchedShows(token.token)
      .filter { it.show != null }
    val localMyShows = database.myShowsDao().getAll()
    val localEpisodes = batchEpisodes(localMyShows.map { it.idTrakt })
      .filter { !hasEpisodeBeenWatched(remoteWatched, it) }

    val request = SyncExportRequest(
      episodes = localEpisodes.map { ep ->
        val timestamp = localMyShows.find { it.idTrakt == ep.idShowTrakt }?.updatedAt ?: nowUtcMillis()
        SyncExportItem.create(ep.idTrakt, dateIsoStringFromMillis(timestamp))
      }
    )

    cloud.traktApi.postSyncWatched(token.token, request)
  }

  private suspend fun exportMoviesWatched(token: TraktAuthToken) {
    Timber.d("Exporting watched movies...")

    val remoteWatched = cloud.traktApi.fetchSyncWatchedMovies(token.token)
      .filter { it.movie != null }
    val localMyMoviesIds = database.myMoviesDao().getAllTraktIds()
    val localMyMovies = batchMovies(localMyMoviesIds)
      .filter { movie -> remoteWatched.none { it.movie?.ids?.trakt == movie.idTrakt } }

    val request = SyncExportRequest(
      movies = localMyMovies.map { movie ->
        val timestamp = movie.updatedAt
        SyncExportItem.create(movie.idTrakt, dateIsoStringFromMillis(timestamp))
      }
    )

    cloud.traktApi.postSyncWatched(token.token, request)
  }

  private suspend fun batchEpisodes(
    showsIds: List<Long>,
    allEpisodes: MutableList<Episode> = mutableListOf()
  ): List<Episode> {
    val batch = showsIds.take(500)
    if (batch.isEmpty()) return allEpisodes

    val episodes = database.episodesDao().getAllWatchedForShows(batch)
    allEpisodes.addAll(episodes)

    return batchEpisodes(showsIds.filter { it !in batch }, allEpisodes)
  }

  private suspend fun batchMovies(
    moviesIds: List<Long>,
    result: MutableList<Movie> = mutableListOf()
  ): List<Movie> {
    val batch = moviesIds.take(500)
    if (batch.isEmpty()) return result

    val movies = database.myMoviesDao().getAll(batch)
    result.addAll(movies)

    return batchMovies(moviesIds.filter { it !in batch }, result)
  }

  private fun hasEpisodeBeenWatched(remoteWatched: List<SyncItem>, episodeDb: Episode): Boolean {
    val find = remoteWatched
      .find { it.show?.ids?.trakt == episodeDb.idShowTrakt }
      ?.seasons?.find { it.number == episodeDb.seasonNumber }
      ?.episodes?.find { it.number == episodeDb.episodeNumber }
    return find != null
  }
}
