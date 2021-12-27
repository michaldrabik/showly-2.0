package com.michaldrabik.ui_show.cases

import com.michaldrabik.common.Mode
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_local.database.model.Season
import com.michaldrabik.data_local.database.model.TraktSyncQueue
import com.michaldrabik.repository.PinnedItemsRepository
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_base.trakt.quicksync.QuickSyncManager
import com.michaldrabik.ui_base.utilities.extensions.runTransaction
import com.michaldrabik.ui_model.Show
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class ShowDetailsHiddenCase @Inject constructor(
  private val database: AppDatabase,
  private val showsRepository: ShowsRepository,
  private val pinnedItemsRepository: PinnedItemsRepository,
  private val quickSyncManager: QuickSyncManager,
) {

  suspend fun isHidden(show: Show) =
    showsRepository.hiddenShows.exists(show.ids.trakt)

  suspend fun addToHidden(show: Show, removeLocalData: Boolean) {
    database.runTransaction {
      showsRepository.hiddenShows.insert(show.ids.trakt)

      if (removeLocalData) {
        episodesDao().deleteAllUnwatchedForShow(show.traktId)
        val seasons = seasonsDao().getAllByShowId(show.traktId)
        val episodes = episodesDao().getAllByShowId(show.traktId)
        val toDelete = mutableListOf<Season>()
        seasons.forEach { season ->
          if (episodes.none { it.idSeason == season.idTrakt }) {
            toDelete.add(season)
          }
        }
        seasonsDao().delete(toDelete)
      }
    }
    pinnedItemsRepository.removePinnedItem(show)
    quickSyncManager.scheduleHidden(show.traktId, Mode.SHOWS, TraktSyncQueue.Operation.ADD)
  }

  suspend fun removeFromHidden(show: Show) =
    showsRepository.hiddenShows.delete(show.ids.trakt)
}
