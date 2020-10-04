package com.michaldrabik.ui_my_shows.archive.cases

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.SortOrder.*
import com.michaldrabik.ui_repository.SettingsRepository
import com.michaldrabik.ui_repository.shows.ShowsRepository
import javax.inject.Inject

@AppScope
class ArchiveLoadShowsCase @Inject constructor(
  private val showsRepository: ShowsRepository,
  private val settingsRepository: SettingsRepository
) {

  suspend fun loadShows(): List<Show> {
    val sortType = settingsRepository.load().archiveShowsSortBy
    val shows = showsRepository.archiveShows.loadAll()
    return when (sortType) {
      NAME -> shows.sortedBy { it.title }
      DATE_ADDED -> shows.sortedByDescending { it.updatedAt }
      RATING -> shows.sortedByDescending { it.rating }
      NEWEST -> shows.sortedByDescending { it.year }
      else -> error("Should not be used here.")
    }
  }
}
