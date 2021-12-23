package com.michaldrabik.ui_base.common.sheets.context_menu.show.cases

import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_local.database.model.Season
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_base.trakt.quicksync.QuickSyncManager
import com.michaldrabik.ui_base.utilities.extensions.runTransaction
import com.michaldrabik.ui_model.IdTrakt
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class ShowContextMenuWatchlistCase @Inject constructor(
  private val database: AppDatabase,
  private val showsRepository: ShowsRepository,
  private val quickSyncManager: QuickSyncManager,
) {

  suspend fun moveToWatchlist(traktId: IdTrakt, removeLocalData: Boolean) {
    val isMyShow = showsRepository.myShows.exists(traktId)

    database.runTransaction {
      showsRepository.watchlistShows.insert(traktId)
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
    }

    quickSyncManager.scheduleShowsWatchlist(listOf(traktId.id))
  }

  suspend fun removeFromWatchlist(traktId: IdTrakt) {
    showsRepository.watchlistShows.delete(traktId)
    quickSyncManager.clearShowsWatchlist(listOf(traktId.id))
  }
}
