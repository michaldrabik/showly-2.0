package com.michaldrabik.ui_show.cases

import com.michaldrabik.repository.PinnedItemsRepository
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_base.notifications.AnnouncementManager
import com.michaldrabik.ui_base.trakt.quicksync.QuickSyncManager
import com.michaldrabik.ui_model.Show
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class ShowDetailsWatchlistCase @Inject constructor(
  private val showsRepository: ShowsRepository,
  private val pinnedItemsRepository: PinnedItemsRepository,
  private val quickSyncManager: QuickSyncManager,
  private val announcementManager: AnnouncementManager,
) {

  suspend fun isWatchlist(show: Show) =
    showsRepository.watchlistShows.exists(show.ids.trakt)

  suspend fun addToWatchlist(show: Show) {
    showsRepository.watchlistShows.insert(show.ids.trakt)
    pinnedItemsRepository.removePinnedItem(show)
    announcementManager.refreshShowsAnnouncements()
    quickSyncManager.scheduleShowsWatchlist(listOf(show.traktId))
  }

  suspend fun removeFromWatchlist(show: Show) {
    showsRepository.watchlistShows.delete(show.ids.trakt)
    pinnedItemsRepository.removePinnedItem(show)
    announcementManager.refreshShowsAnnouncements()
    quickSyncManager.clearWatchlistShows(listOf(show.traktId))
  }
}
