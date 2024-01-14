package com.michaldrabik.ui_base.trakt.quicksync.runners.cases

import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.data_local.database.model.TraktSyncQueue
import com.michaldrabik.data_remote.RemoteDataSource
import com.michaldrabik.data_remote.trakt.model.SyncItem
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class QuickSyncDuplicateMoviesCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val remoteSource: RemoteDataSource
) {

  suspend fun checkDuplicateMovies(
    exportMovies: List<TraktSyncQueue>,
    fetchedSyncItems: List<SyncItem>
  ): Result {
    if (exportMovies.isEmpty()) {
      return Result(emptyList(), fetchedSyncItems)
    }
    return withContext(dispatchers.IO) {
      val remoteMovies = if (fetchedSyncItems.isNotEmpty()) {
        fetchedSyncItems.toList()
      } else {
        remoteSource.trakt.fetchSyncWatchedMovies()
      }
      val duplicateMoviesIds = mutableListOf<Long>()

      exportMovies.forEach { movie ->
        remoteMovies
          .find { it.getTraktId() == movie.idTrakt }
          ?.let {
            duplicateMoviesIds.add(movie.idTrakt)
          }
      }

      Timber.d("Duplicated movies count: ${duplicateMoviesIds.size}")

      Result(
        duplicateMoviesIds = duplicateMoviesIds,
        remoteMovies = remoteMovies
      )
    }
  }

  data class Result(
    val duplicateMoviesIds: List<Long>,
    val remoteMovies: List<SyncItem>
  )
}
