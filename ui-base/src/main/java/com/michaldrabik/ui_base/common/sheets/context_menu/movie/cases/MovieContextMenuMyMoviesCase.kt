package com.michaldrabik.ui_base.common.sheets.context_menu.movie.cases

import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.repository.PinnedItemsRepository
import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.ui_base.common.sheets.context_menu.events.RemoveTraktUiEvent
import com.michaldrabik.ui_base.notifications.AnnouncementManager
import com.michaldrabik.ui_base.trakt.quicksync.QuickSyncManager
import com.michaldrabik.ui_base.utilities.extensions.runTransaction
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Ids
import com.michaldrabik.ui_model.Movie
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

@ViewModelScoped
class MovieContextMenuMyMoviesCase @Inject constructor(
  private val database: AppDatabase,
  private val moviesRepository: MoviesRepository,
  private val pinnedItemsRepository: PinnedItemsRepository,
  private val announcementManager: AnnouncementManager,
  private val quickSyncManager: QuickSyncManager,
) {

  suspend fun moveToMyMovies(traktId: IdTrakt) = coroutineScope {
    val (isWatchlist, isHidden) = awaitAll(
      async { moviesRepository.watchlistMovies.exists(traktId) },
      async { moviesRepository.hiddenMovies.exists(traktId) }
    )

    database.runTransaction {
      with(moviesRepository) {
        myMovies.insert(traktId)
        watchlistMovies.delete(traktId)
        hiddenMovies.delete(traktId)
      }
    }

    with(quickSyncManager) {
      clearWatchlistMovies(listOf(traktId.id))
      clearHiddenMovies(listOf(traktId.id))
      scheduleMovies(listOf(traktId.id))
    }

    announcementManager.refreshMoviesAnnouncements()

    RemoveTraktUiEvent(removeWatchlist = isWatchlist, removeHidden = isHidden)
  }

  suspend fun removeFromMyMovies(traktId: IdTrakt) {
    val movie = Movie.EMPTY.copy(ids = Ids.EMPTY.copy(traktId))
    moviesRepository.myMovies.delete(traktId)
    pinnedItemsRepository.removePinnedItem(movie)
    quickSyncManager.clearMovies(listOf(traktId.id))
  }
}
