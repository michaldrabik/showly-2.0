package com.michaldrabik.ui_base.trakt.exports

import com.michaldrabik.common.extensions.dateIsoStringFromMillis
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_local.database.model.Episode
import com.michaldrabik.data_local.database.model.Movie
import com.michaldrabik.data_remote.Cloud
import com.michaldrabik.data_remote.trakt.model.SyncExportItem
import com.michaldrabik.data_remote.trakt.model.SyncExportRequest
import com.michaldrabik.data_remote.trakt.model.SyncItem
import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.repository.TraktAuthToken
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.ui_base.trakt.TraktSyncRunner
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraktExportWatchedRunner @Inject constructor(
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
      exportWatched(authToken)
    } catch (error: Throwable) {
      if (retryCount < MAX_RETRY_COUNT) {
        Timber.w("exportWatched failed. Will retry in $RETRY_DELAY_MS ms... $error")
        retryCount += 1
        delay(RETRY_DELAY_MS)
        runExport(authToken)
      } else {
        isRunning = false
        throw error
      }
    }
  }

  private suspend fun exportWatched(token: TraktAuthToken) {
    Timber.d("Exporting watched...")

    val remoteShows = cloud.traktApi.fetchSyncWatchedShows(token.token)
      .filter { it.show != null }
    val localMyShows = database.myShowsDao().getAll()
    val localEpisodes = batchEpisodes(localMyShows.map { it.idTrakt })
      .filter { !hasEpisodeBeenWatched(remoteShows, it) }

    val movies = mutableListOf<SyncExportItem>()
    if (settingsRepository.isMoviesEnabled) {
      val remoteMovies = cloud.traktApi.fetchSyncWatchedMovies(token.token)
        .filter { it.movie != null }
      val localMyMoviesIds = database.myMoviesDao().getAllTraktIds()
      val localMyMovies = batchMovies(localMyMoviesIds)
        .filter { movie -> remoteMovies.none { it.movie?.ids?.trakt == movie.idTrakt } }

      localMyMovies.mapTo(movies) {
        val timestamp = it.updatedAt
        SyncExportItem.create(it.idTrakt, dateIsoStringFromMillis(timestamp))
      }
    }

    val request = SyncExportRequest(
      episodes = localEpisodes.map { ep ->
        val timestamp = localMyShows.find { it.idTrakt == ep.idShowTrakt }?.updatedAt ?: nowUtcMillis()
        SyncExportItem.create(ep.idTrakt, dateIsoStringFromMillis(timestamp))
      },
      movies = movies
    )

    Timber.d("Exporting ${localEpisodes.size} episodes & ${movies.size} movies...")
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
