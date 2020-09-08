package com.michaldrabik.showly2.ui.show.cases

import androidx.room.withTransaction
import com.michaldrabik.showly2.di.scope.AppScope
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.repository.PinnedItemsRepository
import com.michaldrabik.showly2.repository.shows.ShowsRepository
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.Season
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
        val episodes = database.episodesDao().getAllForShows(listOf(show.ids.trakt.id))
        val toDelete = mutableListOf<Season>()
        seasons.forEach { season ->
          if (episodes.none { it.idSeason == season.idTrakt }) {
            toDelete.add(season)
          }
        }
        database.seasonsDao().delete(toDelete)
      }
    }
    pinnedItemsRepository.removePinnedItem(show.traktId)
  }

  suspend fun removeFromArchive(show: Show) =
    showsRepository.archiveShows.delete(show.ids.trakt)
}
