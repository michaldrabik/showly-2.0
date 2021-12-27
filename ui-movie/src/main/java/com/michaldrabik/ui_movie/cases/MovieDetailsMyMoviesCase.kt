package com.michaldrabik.ui_movie.cases

import com.michaldrabik.repository.PinnedItemsRepository
import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.ui_base.notifications.AnnouncementManager
import com.michaldrabik.ui_base.trakt.quicksync.QuickSyncManager
import com.michaldrabik.ui_model.Movie
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

@ViewModelScoped
class MovieDetailsMyMoviesCase @Inject constructor(
  private val moviesRepository: MoviesRepository,
  private val pinnedItemsRepository: PinnedItemsRepository,
  private val quickSyncManager: QuickSyncManager,
  private val announcementManager: AnnouncementManager
) {

  suspend fun getAllIds() = coroutineScope {
    val (myMovies, watchlistMovies) = awaitAll(
      async { moviesRepository.myMovies.loadAllIds() },
      async { moviesRepository.watchlistMovies.loadAllIds() }
    )
    Pair(myMovies, watchlistMovies)
  }

  suspend fun isMyMovie(movie: Movie) =
    moviesRepository.myMovies.load(movie.ids.trakt) != null

  suspend fun addToMyMovies(movie: Movie) {
    moviesRepository.myMovies.insert(movie.ids.trakt)
    quickSyncManager.scheduleMovies(listOf(movie.traktId))
    pinnedItemsRepository.removePinnedItem(movie)
    announcementManager.refreshMoviesAnnouncements()
  }

  suspend fun removeFromMyMovies(movie: Movie) {
    moviesRepository.myMovies.delete(movie.ids.trakt)
    pinnedItemsRepository.removePinnedItem(movie)
    quickSyncManager.clearMovies(listOf(movie.traktId))
  }
}
