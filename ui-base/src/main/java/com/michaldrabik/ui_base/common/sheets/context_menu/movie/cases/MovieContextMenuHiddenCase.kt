package com.michaldrabik.ui_base.common.sheets.context_menu.movie.cases

import com.michaldrabik.common.Mode
import com.michaldrabik.data_local.database.model.TraktSyncQueue
import com.michaldrabik.repository.PinnedItemsRepository
import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.ui_base.common.sheets.context_menu.events.RemoveTraktUiEvent
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
class MovieContextMenuHiddenCase @Inject constructor(
  private val moviesRepository: MoviesRepository,
  private val pinnedItemsRepository: PinnedItemsRepository,
  private val quickSyncManager: QuickSyncManager,
) {

  suspend fun moveToHidden(traktId: IdTrakt) = coroutineScope {
    val movie = Movie.EMPTY.copy(ids = Ids.EMPTY.copy(traktId))

    val (isMyMovie, isWatchlist) = awaitAll(
      async { moviesRepository.myMovies.exists(traktId) },
      async { moviesRepository.watchlistMovies.exists(traktId) }
    )

    moviesRepository.hiddenMovies.insert(movie.ids.trakt)
    pinnedItemsRepository.removePinnedItem(movie)
    with(quickSyncManager) {
      clearMovies(listOf(traktId.id))
      clearWatchlistMovies(listOf(traktId.id))
      scheduleHidden(traktId.id, Mode.MOVIES, TraktSyncQueue.Operation.ADD)
    }

    RemoveTraktUiEvent(removeProgress = isMyMovie, removeWatchlist = isWatchlist)
  }

  suspend fun removeFromHidden(traktId: IdTrakt) {
    moviesRepository.hiddenMovies.delete(traktId)
    quickSyncManager.clearHiddenMovies(listOf(traktId.id))
  }
}
