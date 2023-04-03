package com.michaldrabik.ui_movie.cases

import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.repository.PinnedItemsRepository
import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.ui_base.notifications.AnnouncementManager
import com.michaldrabik.ui_base.trakt.quicksync.QuickSyncManager
import com.michaldrabik.ui_model.Movie
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class MovieDetailsMyMoviesCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val moviesRepository: MoviesRepository,
  private val pinnedItemsRepository: PinnedItemsRepository,
  private val quickSyncManager: QuickSyncManager,
  private val announcementManager: AnnouncementManager
) {

  suspend fun getAllIds() = withContext(dispatchers.IO) {
    val (myMovies, watchlistMovies) = awaitAll(
      async { moviesRepository.myMovies.loadAllIds() },
      async { moviesRepository.watchlistMovies.loadAllIds() }
    )
    Pair(myMovies, watchlistMovies)
  }

  suspend fun isMyMovie(movie: Movie) = withContext(dispatchers.IO) {
    moviesRepository.myMovies.load(movie.ids.trakt) != null
  }

  suspend fun addToMyMovies(movie: Movie) {
    withContext(dispatchers.IO) {
      moviesRepository.myMovies.insert(movie.ids.trakt)
      quickSyncManager.scheduleMovies(listOf(movie.traktId))
      pinnedItemsRepository.removePinnedItem(movie)
      announcementManager.refreshMoviesAnnouncements()
    }
  }

  suspend fun removeFromMyMovies(movie: Movie) {
    withContext(dispatchers.IO) {
      moviesRepository.myMovies.delete(movie.ids.trakt)
      pinnedItemsRepository.removePinnedItem(movie)
      quickSyncManager.clearMovies(listOf(movie.traktId))
    }
  }
}
