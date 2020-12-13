package com.michaldrabik.showly2.common.movies

import com.michaldrabik.common.Config.MOVIE_SYNC_COOLDOWN
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.ui_model.MovieStatus.IN_PRODUCTION
import com.michaldrabik.ui_model.MovieStatus.PLANNED
import com.michaldrabik.ui_model.MovieStatus.POST_PRODUCTION
import com.michaldrabik.ui_model.MovieStatus.RUMORED
import com.michaldrabik.ui_repository.SettingsRepository
import com.michaldrabik.ui_repository.movies.MoviesRepository
import timber.log.Timber
import javax.inject.Inject

/**
 * This class is responsible for fetching and syncing missing/updated movies data.
 */
@AppScope
class MoviesSyncRunner @Inject constructor(
  private val database: AppDatabase,
  private val moviesRepository: MoviesRepository,
  private val settingsRepository: SettingsRepository
) {

  suspend fun run(): Int {
    Timber.i("Movies sync initialized.")

    if (!settingsRepository.isMoviesEnabled()) {
      Timber.i("Movies are disabled. Exiting...")
      return 0
    }

    val movies = moviesRepository.loadCollection()

    val moviesToSync = movies.filter { it.status in arrayOf(PLANNED, IN_PRODUCTION, POST_PRODUCTION, RUMORED) }
    if (moviesToSync.isEmpty()) {
      Timber.i("Nothing to process. Exiting...")
      return 0
    }
    Timber.i("Movies to sync count: ${moviesToSync.size}.")

    var syncCount = 0
    val syncLog = database.moviesSyncLogDao().getAll()
    moviesToSync.forEach { movie ->
      val lastSync = syncLog.find { it.idTrakt == movie.ids.trakt.id }?.syncedAt ?: 0
      if (nowUtcMillis() - lastSync < MOVIE_SYNC_COOLDOWN) {
        Timber.i("${movie.title} is on cooldown. No need to sync.")
        return@forEach
      }

      try {
        Timber.i("Syncing ${movie.title}(${movie.ids.trakt}) details...")
        moviesRepository.movieDetails.load(movie.ids.trakt, force = true)
        syncCount++
        Timber.i("${movie.title}(${movie.ids.trakt}) movie synced.")
      } catch (t: Throwable) {
        Timber.e("${movie.title}(${movie.ids.trakt}) movie sync error. Skipping... \n$t")
      }
    }

    return syncCount
  }
}
