package com.michaldrabik.ui_base.common.sheets.context_menu.show.cases

import com.michaldrabik.common.Mode
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_local.database.model.Season
import com.michaldrabik.data_local.database.model.TraktSyncQueue
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
class ShowContextMenuHiddenCase @Inject constructor(
  private val localSource: LocalDataSource,
  private val transactions: TransactionsProvider,
  private val showsRepository: ShowsRepository,
  private val pinnedItemsRepository: PinnedItemsRepository,
  private val quickSyncManager: QuickSyncManager,
  private val announcementManager: AnnouncementManager,
) {

  suspend fun moveToHidden(traktId: IdTrakt, removeLocalData: Boolean) = coroutineScope {
    val show = Show.EMPTY.copy(ids = Ids.EMPTY.copy(traktId))

    val (isMyShow, isWatchlist) = awaitAll(
      async { showsRepository.myShows.exists(traktId) },
      async { showsRepository.watchlistShows.exists(traktId) }
    )

    transactions.withTransaction {
      showsRepository.hiddenShows.insert(show.ids.trakt)

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
      clearWatchlistShows(listOf(traktId.id))
      scheduleHidden(traktId.id, Mode.SHOWS, TraktSyncQueue.Operation.ADD)
    }

    RemoveTraktUiEvent(removeProgress = isMyShow, removeWatchlist = isWatchlist)
  }

  suspend fun removeFromHidden(traktId: IdTrakt) {
    showsRepository.hiddenShows.delete(traktId)
    announcementManager.refreshShowsAnnouncements()
    quickSyncManager.clearHiddenShows(listOf(traktId.id))
  }
}
