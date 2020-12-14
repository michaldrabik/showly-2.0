package com.michaldrabik.ui_progress_movies.main.cases

import android.content.Context
import androidx.room.withTransaction
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.ui_base.trakt.quicksync.QuickSyncManager
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_repository.PinnedItemsRepository
import com.michaldrabik.ui_repository.movies.MoviesRepository
import javax.inject.Inject

@AppScope
class ProgressMoviesMainCase @Inject constructor(
  private val database: AppDatabase,
  private val moviesRepository: MoviesRepository,
  private val pinnedItemsRepository: PinnedItemsRepository,
  private val quickSyncManager: QuickSyncManager
) {

  suspend fun addToMyMovies(context: Context, movie: Movie) {
    database.withTransaction {
      moviesRepository.myMovies.insert(movie.ids.trakt)
      moviesRepository.watchlistMovies.delete(movie.ids.trakt)
    }
    pinnedItemsRepository.removePinnedItem(movie)
    quickSyncManager.scheduleMovies(context, listOf(movie.ids.trakt.id))
  }
}
