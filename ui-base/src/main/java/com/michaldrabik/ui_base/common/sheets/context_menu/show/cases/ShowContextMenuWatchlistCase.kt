package com.michaldrabik.ui_base.common.sheets.context_menu.show.cases

import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_local.database.model.Season
import com.michaldrabik.repository.PinnedItemsRepository
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_base.common.sheets.context_menu.events.RemoveTraktUiEvent
import com.michaldrabik.ui_base.trakt.quicksync.QuickSyncManager
import com.michaldrabik.ui_base.utilities.extensions.runTransaction
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Ids
import com.michaldrabik.ui_model.Show
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

@ViewModelScoped
class ShowContextMenuWatchlistCase @Inject constructor(
  private val database: AppDatabase,
  private val showsRepository: ShowsRepository,
  private val pinnedItemsRepository: PinnedItemsRepository,
  private val quickSyncManager: QuickSyncManager,
) {

  suspend fun moveToWatchlist(traktId: IdTrakt, removeLocalData: Boolean) = coroutineScope {
    val show = Show.EMPTY.copy(ids = Ids.EMPTY.copy(traktId))

    val (isMyShow, isHidden) = awaitAll(
      async { showsRepository.myShows.exists(traktId) },
      async { showsRepository.hiddenShows.exists(traktId) }
    )

    database.runTransaction {
      with(showsRepository) {
        watchlistShows.insert(show.ids.trakt)
        myShows.delete(show.ids.trakt)
        hiddenShows.delete(show.ids.trakt)
      }

      if (removeLocalData && isMyShow) {
        episodesDao().deleteAllUnwatchedForShow(traktId.id)
        val seasons = seasonsDao().getAllByShowId(traktId.id)
        val episodes = episodesDao().getAllByShowId(traktId.id)
        val toDelete = mutableListOf<Season>()
        seasons.forEach { season ->
          if (episodes.none { it.idSeason == season.idTrakt }) {
            toDelete.add(season)
          }
        }
        seasonsDao().delete(toDelete)
      }
      pinnedItemsRepository.removePinnedItem(show)
    }

    with(quickSyncManager) {
      clearHiddenShows(listOf(traktId.id))
      scheduleShowsWatchlist(listOf(traktId.id))
    }

    RemoveTraktUiEvent(removeProgress = isMyShow, removeHidden = isHidden)
  }

  suspend fun removeFromWatchlist(traktId: IdTrakt) {
    showsRepository.watchlistShows.delete(traktId)
    quickSyncManager.clearWatchlistShows(listOf(traktId.id))
  }
}
