package com.michaldrabik.ui_show.cases

import androidx.room.withTransaction
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_local.database.model.Season
import com.michaldrabik.repository.PinnedItemsRepository
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_model.Show
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class ShowDetailsArchiveCase @Inject constructor(
  private val database: AppDatabase,
  private val showsRepository: ShowsRepository,
  private val pinnedItemsRepository: PinnedItemsRepository
) {

  suspend fun isArchived(show: Show) =
    showsRepository.archiveShows.isArchived(show.ids.trakt)

  suspend fun addToArchive(show: Show, removeLocalData: Boolean) {
    database.withTransaction {
      showsRepository.archiveShows.insert(show.ids.trakt)
      if (removeLocalData) {
        database.episodesDao().deleteAllUnwatchedForShow(show.traktId)
        val seasons = database.seasonsDao().getAllByShowId(show.traktId)
        val episodes = database.episodesDao().getAllByShowId(show.traktId)
        val toDelete = mutableListOf<Season>()
        seasons.forEach { season ->
          if (episodes.none { it.idSeason == season.idTrakt }) {
            toDelete.add(season)
          }
        }
        database.seasonsDao().delete(toDelete)
      }
    }
    pinnedItemsRepository.removePinnedItem(show)
  }

  suspend fun removeFromArchive(show: Show) =
    showsRepository.archiveShows.delete(show.ids.trakt)
}
