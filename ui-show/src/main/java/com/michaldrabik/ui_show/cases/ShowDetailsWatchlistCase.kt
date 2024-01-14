package com.michaldrabik.ui_show.cases

import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.repository.PinnedItemsRepository
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_base.notifications.AnnouncementManager
import com.michaldrabik.ui_base.trakt.quicksync.QuickSyncManager
import com.michaldrabik.ui_model.Show
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class ShowDetailsWatchlistCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val showsRepository: ShowsRepository,
  private val pinnedItemsRepository: PinnedItemsRepository,
  private val quickSyncManager: QuickSyncManager,
  private val announcementManager: AnnouncementManager,
) {

  suspend fun isWatchlist(show: Show) = withContext(dispatchers.IO) {
    showsRepository.watchlistShows.exists(show.ids.trakt)
  }

  suspend fun addToWatchlist(show: Show) = withContext(dispatchers.IO) {
    showsRepository.watchlistShows.insert(show.ids.trakt)
    pinnedItemsRepository.removePinnedItem(show)
    announcementManager.refreshShowsAnnouncements()
    quickSyncManager.scheduleShowsWatchlist(listOf(show.traktId))
  }

  suspend fun removeFromWatchlist(show: Show) = withContext(dispatchers.IO) {
    showsRepository.watchlistShows.delete(show.ids.trakt)
    pinnedItemsRepository.removePinnedItem(show)
    announcementManager.refreshShowsAnnouncements()
    quickSyncManager.clearWatchlistShows(listOf(show.traktId))
  }
}
