package com.michaldrabik.showly2.ui.show.cases

import com.michaldrabik.showly2.di.scope.AppScope
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.repository.shows.ShowsRepository
import com.michaldrabik.storage.database.AppDatabase
import javax.inject.Inject

@AppScope
class ShowDetailsArchiveCase @Inject constructor(
  private val database: AppDatabase,
  private val showsRepository: ShowsRepository
) {

  suspend fun isArchived(show: Show) =
    showsRepository.archiveShows.isArchived(show.ids.trakt)

  suspend fun addToArchive(show: Show) =
    showsRepository.archiveShows.insert(show.ids.trakt)

  suspend fun removeFromArchive(show: Show) =
    showsRepository.archiveShows.delete(show.ids.trakt)
}
