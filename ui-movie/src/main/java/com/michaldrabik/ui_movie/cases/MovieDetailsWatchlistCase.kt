package com.michaldrabik.ui_movie.cases

import com.michaldrabik.repository.PinnedItemsRepository
import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.ui_base.notifications.AnnouncementManager
import com.michaldrabik.ui_base.trakt.quicksync.QuickSyncManager
import com.michaldrabik.ui_model.Movie
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class MovieDetailsWatchlistCase @Inject constructor(
  private val moviesRepository: MoviesRepository,
  private val pinnedItemsRepository: PinnedItemsRepository,
  private val quickSyncManager: QuickSyncManager,
  private val announcementManager: AnnouncementManager
) {

  suspend fun isWatchlist(movie: Movie) =
    moviesRepository.watchlistMovies.load(movie.ids.trakt) != null

  suspend fun addToWatchlist(movie: Movie) {
    moviesRepository.watchlistMovies.insert(movie.ids.trakt)
    pinnedItemsRepository.removePinnedItem(movie)
    quickSyncManager.scheduleMoviesWatchlist(listOf(movie.traktId))
    announcementManager.refreshMoviesAnnouncements()
  }

  suspend fun removeFromWatchlist(movie: Movie) {
    moviesRepository.watchlistMovies.delete(movie.ids.trakt)
    pinnedItemsRepository.removePinnedItem(movie)
    quickSyncManager.clearWatchlistMovies(listOf(movie.traktId))
  }
}
