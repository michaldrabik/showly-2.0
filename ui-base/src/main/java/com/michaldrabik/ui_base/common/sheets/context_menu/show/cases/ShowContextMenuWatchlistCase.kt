package com.michaldrabik.ui_base.common.sheets.context_menu.show.cases

import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_local.database.model.Season
import com.michaldrabik.data_local.utilities.TransactionsProvider
import com.michaldrabik.repository.PinnedItemsRepository
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_base.common.sheets.context_menu.events.RemoveTraktUiEvent
import com.michaldrabik.ui_base.notifications.AnnouncementManager
import com.michaldrabik.ui_base.trakt.quicksync.QuickSyncManager
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
  private val localSource: LocalDataSource,
  private val transactions: TransactionsProvider,
  private val showsRepository: ShowsRepository,
  private val pinnedItemsRepository: PinnedItemsRepository,
  private val quickSyncManager: QuickSyncManager,
  private val announcementManager: AnnouncementManager,
) {

  suspend fun moveToWatchlist(
    traktId: IdTrakt,
    removeLocalData: Boolean,
  ) = coroutineScope {
    val show = Show.EMPTY.copy(ids = Ids.EMPTY.copy(traktId))

    val (isMyShow, isHidden) = awaitAll(
      async { showsRepository.myShows.exists(traktId) },
      async { showsRepository.hiddenShows.exists(traktId) }
    )

    transactions.withTransaction {
      showsRepository.watchlistShows.insert(show.ids.trakt)

      if (removeLocalData && isMyShow) {
        localSource.episodes.deleteAllUnwatchedForShow(traktId.id)
        val seasons = localSource.seasons.getAllByShowId(traktId.id)
        val episodes = localSource.episodes.getAllByShowId(traktId.id)
        val toDelete = mutableListOf<Season>()
        seasons.forEach { season ->
          if (episodes.none { it.idSeason == season.idTrakt }) {
            toDelete.add(season)
          }
        }
        localSource.seasons.delete(toDelete)
      }
    }

    pinnedItemsRepository.removePinnedItem(show)
    announcementManager.refreshShowsAnnouncements()
    with(quickSyncManager) {
      clearHiddenShows(listOf(traktId.id))
      scheduleShowsWatchlist(listOf(traktId.id))
    }

    RemoveTraktUiEvent(removeProgress = isMyShow, removeHidden = isHidden)
  }

  suspend fun removeFromWatchlist(traktId: IdTrakt) {
    showsRepository.watchlistShows.delete(traktId)
    announcementManager.refreshShowsAnnouncements()
    quickSyncManager.clearWatchlistShows(listOf(traktId.id))
  }
}
