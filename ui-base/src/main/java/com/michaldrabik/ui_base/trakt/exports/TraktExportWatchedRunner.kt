package com.michaldrabik.ui_base.trakt.exports

import com.michaldrabik.common.extensions.dateIsoStringFromMillis
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_local.database.model.Episode
import com.michaldrabik.data_local.database.model.Movie
import com.michaldrabik.data_local.database.model.Show
import com.michaldrabik.data_remote.RemoteDataSource
import com.michaldrabik.data_remote.trakt.model.SyncExportItem
import com.michaldrabik.data_remote.trakt.model.SyncExportRequest
import com.michaldrabik.data_remote.trakt.model.SyncItem
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.trakt.TraktSyncRunner
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraktExportWatchedRunner @Inject constructor(
  private val remoteSource: RemoteDataSource,
  private val localSource: LocalDataSource,
  private val settingsRepository: SettingsRepository,
  userTraktManager: UserTraktManager
) : TraktSyncRunner(userTraktManager) {

  override suspend fun run(): Int {
    Timber.d("Initialized.")
    isRunning = true

    checkAuthorization()
    runExport()

    isRunning = false
    Timber.d("Finished with success.")
    return 0
  }

  private suspend fun runExport() {
    try {
      exportWatched()
    } catch (error: Throwable) {
      if (retryCount < MAX_RETRY_COUNT) {
        Timber.w("exportWatched failed. Will retry in $RETRY_DELAY_MS ms... $error")
        retryCount += 1
        delay(RETRY_DELAY_MS)
        runExport()
      } else {
        isRunning = false
        throw error
      }
    }
  }

  private suspend fun exportWatched() {
    Timber.d("Exporting watched...")

    val remoteShows = remoteSource.trakt.fetchSyncWatchedShows()
      .filter { it.show != null }
    val localMyShows = localSource.myShows.getAll()
    val localEpisodes = batchEpisodes(localMyShows.map { it.idTrakt })
      .filter { !hasEpisodeBeenWatched(remoteShows, it) }

    val movies = mutableListOf<SyncExportItem>()
    if (settingsRepository.isMoviesEnabled) {
      val remoteMovies = remoteSource.trakt.fetchSyncWatchedMovies()
        .filter { it.movie != null }
      val localMyMoviesIds = localSource.myMovies.getAllTraktIds()
      val localMyMovies = batchMovies(localMyMoviesIds)
        .filter { movie -> remoteMovies.none { it.movie?.ids?.trakt == movie.idTrakt } }

      localMyMovies.mapTo(movies) {
        SyncExportItem.create(it.idTrakt, dateIsoStringFromMillis(it.updatedAt))
      }
    }

    val request = SyncExportRequest(
      episodes = localEpisodes.map { ep ->
        val showTimestamp = localMyShows.find { it.idTrakt == ep.idShowTrakt }?.updatedAt ?: 0
        val timestamp = when {
          showTimestamp > 0 -> showTimestamp
          else -> nowUtcMillis()
        }
        SyncExportItem.create(ep.idTrakt, dateIsoStringFromMillis(timestamp))
      },
      movies = movies
    )

    Timber.d("Exporting ${localEpisodes.size} episodes & ${movies.size} movies...")
    remoteSource.trakt.postSyncWatched(request)

    exportHidden()
  }

  private suspend fun exportHidden() = coroutineScope {
    Timber.d("Exporting hidden items...")

    val showsAsync = async { localSource.archiveShows.getAll() }
    val moviesAsync = async { localSource.archiveMovies.getAll() }
    val (localShows, localMovies) = awaitAll(showsAsync, moviesAsync)

    val showsItems = localShows.map {
      (it as Show).let { show -> SyncExportItem.create(show.idTrakt, hiddenAt = dateIsoStringFromMillis(show.updatedAt)) }
    }
    val moviesItems = localMovies.map {
      (it as Movie).let { movie -> SyncExportItem.create(movie.idTrakt, hiddenAt = dateIsoStringFromMillis(movie.updatedAt)) }
    }

    if (localShows.isNotEmpty()) {
      Timber.d("Exporting ${localShows.size} hidden shows...")
      remoteSource.trakt.postHiddenShows(shows = showsItems)
      delay(1500)
    }

    if (localMovies.isNotEmpty()) {
      Timber.d("Exporting ${localMovies.size} hidden movies...")
      remoteSource.trakt.postHiddenMovies(movies = moviesItems)
    }
  }

  private suspend fun batchEpisodes(
    showsIds: List<Long>,
    allEpisodes: MutableList<Episode> = mutableListOf()
  ): List<Episode> {
    val batch = showsIds.take(500)
    if (batch.isEmpty()) return allEpisodes

    val episodes = localSource.episodes.getAllWatchedForShows(batch)
    allEpisodes.addAll(episodes)

    return batchEpisodes(showsIds.filter { it !in batch }, allEpisodes)
  }

  private suspend fun batchMovies(
    moviesIds: List<Long>,
    result: MutableList<Movie> = mutableListOf()
  ): List<Movie> {
    val batch = moviesIds.take(500)
    if (batch.isEmpty()) return result

    val movies = localSource.myMovies.getAll(batch)
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
