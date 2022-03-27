package com.michaldrabik.ui_base.common.sheets.context_menu.movie.cases

import com.michaldrabik.repository.PinnedItemsRepository
import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.ui_base.common.sheets.context_menu.events.RemoveTraktUiEvent
import com.michaldrabik.ui_base.notifications.AnnouncementManager
import com.michaldrabik.ui_base.trakt.quicksync.QuickSyncManager
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Ids
import com.michaldrabik.ui_model.Movie
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

@ViewModelScoped
class MovieContextMenuWatchlistCase @Inject constructor(
  private val moviesRepository: MoviesRepository,
  private val pinnedItemsRepository: PinnedItemsRepository,
  private val announcementManager: AnnouncementManager,
  private val quickSyncManager: QuickSyncManager,
) {

  suspend fun moveToWatchlist(traktId: IdTrakt) = coroutineScope {
    val movie = Movie.EMPTY.copy(ids = Ids.EMPTY.copy(traktId))

    val (isMyMovie, isHidden) = awaitAll(
      async { moviesRepository.myMovies.exists(traktId) },
      async { moviesRepository.hiddenMovies.exists(traktId) }
    )

    moviesRepository.watchlistMovies.insert(movie.ids.trakt)
    pinnedItemsRepository.removePinnedItem(movie)
    announcementManager.refreshMoviesAnnouncements()

    with(quickSyncManager) {
      clearMovies(listOf(traktId.id))
      clearHiddenMovies(listOf(traktId.id))
      scheduleMoviesWatchlist(listOf(traktId.id))
    }

    RemoveTraktUiEvent(removeProgress = isMyMovie, removeHidden = isHidden)
  }

  suspend fun removeFromWatchlist(traktId: IdTrakt) {
    moviesRepository.watchlistMovies.delete(traktId)
    quickSyncManager.clearWatchlistMovies(listOf(traktId.id))
  }
}
