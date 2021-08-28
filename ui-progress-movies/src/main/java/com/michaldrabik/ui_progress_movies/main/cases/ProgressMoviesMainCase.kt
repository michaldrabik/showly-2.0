package com.michaldrabik.ui_progress_movies.main.cases

import android.content.Context
import androidx.room.withTransaction
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.repository.PinnedItemsRepository
import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.ui_base.trakt.quicksync.QuickSyncManager
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Ids
import com.michaldrabik.ui_model.Movie
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
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
    quickSyncManager.scheduleMovies(listOf(movie.ids.trakt.id))
  }

  suspend fun addToMyMovies(context: Context, movieId: IdTrakt) {
    val movie = Movie.EMPTY.copy(Ids.EMPTY.copy(trakt = movieId))
    addToMyMovies(context, movie)
  }
}
