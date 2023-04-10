package com.michaldrabik.ui_movie.cases

import com.michaldrabik.common.Mode
import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.data_local.database.model.TraktSyncQueue
import com.michaldrabik.repository.PinnedItemsRepository
import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.ui_base.trakt.quicksync.QuickSyncManager
import com.michaldrabik.ui_model.Movie
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class MovieDetailsHiddenCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val moviesRepository: MoviesRepository,
  private val pinnedItemsRepository: PinnedItemsRepository,
  private val quickSyncManager: QuickSyncManager
) {

  suspend fun isHidden(movie: Movie) = withContext(dispatchers.IO) {
    moviesRepository.hiddenMovies.exists(movie.ids.trakt)
  }

  suspend fun addToHidden(movie: Movie) {
    withContext(dispatchers.IO) {
      moviesRepository.hiddenMovies.insert(movie.ids.trakt)
      pinnedItemsRepository.removePinnedItem(movie)
      quickSyncManager.scheduleHidden(movie.traktId, Mode.MOVIES, TraktSyncQueue.Operation.ADD)
    }
  }

  suspend fun removeFromHidden(movie: Movie) {
    withContext(dispatchers.IO) {
      moviesRepository.hiddenMovies.delete(movie.ids.trakt)
      pinnedItemsRepository.removePinnedItem(movie)
      quickSyncManager.clearHiddenMovies(listOf(movie.traktId))
    }
  }
}
