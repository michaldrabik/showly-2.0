package com.michaldrabik.ui_show.cases

import androidx.room.withTransaction
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.Season
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_repository.PinnedItemsRepository
import com.michaldrabik.ui_repository.shows.ShowsRepository
import javax.inject.Inject

@AppScope
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
        database.episodesDao().deleteAllUnwatchedForShow(show.ids.trakt.id)
        val seasons = database.seasonsDao().getAllByShowId(show.ids.trakt.id)
        val episodes = database.episodesDao().getAllByShowId(show.ids.trakt.id)
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
