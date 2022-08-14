package com.michaldrabik.ui_show.cases

import com.michaldrabik.common.Mode
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_local.database.model.Season
import com.michaldrabik.data_local.database.model.TraktSyncQueue
import com.michaldrabik.data_local.utilities.TransactionsProvider
import com.michaldrabik.repository.PinnedItemsRepository
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_base.notifications.AnnouncementManager
import com.michaldrabik.ui_base.trakt.quicksync.QuickSyncManager
import com.michaldrabik.ui_model.Show
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class ShowDetailsHiddenCase @Inject constructor(
  private val localSource: LocalDataSource,
  private val transactions: TransactionsProvider,
  private val showsRepository: ShowsRepository,
  private val pinnedItemsRepository: PinnedItemsRepository,
  private val quickSyncManager: QuickSyncManager,
  private val announcementManager: AnnouncementManager,
) {

  suspend fun isHidden(show: Show) =
    showsRepository.hiddenShows.exists(show.ids.trakt)

  suspend fun addToHidden(show: Show, removeLocalData: Boolean) {
    transactions.withTransaction {
      showsRepository.hiddenShows.insert(show.ids.trakt)

      if (removeLocalData) {
        localSource.episodes.deleteAllUnwatchedForShow(show.traktId)
        val seasons = localSource.seasons.getAllByShowId(show.traktId)
        val episodes = localSource.episodes.getAllByShowId(show.traktId)
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
    quickSyncManager.scheduleHidden(show.traktId, Mode.SHOWS, TraktSyncQueue.Operation.ADD)
  }

  suspend fun removeFromHidden(show: Show) {
    showsRepository.hiddenShows.delete(show.ids.trakt)
    pinnedItemsRepository.removePinnedItem(show)
    announcementManager.refreshShowsAnnouncements()
    quickSyncManager.clearHiddenShows(listOf(show.traktId))
  }
}
