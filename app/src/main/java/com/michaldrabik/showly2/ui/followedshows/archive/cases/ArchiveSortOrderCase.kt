package com.michaldrabik.showly2.ui.followedshows.archive.cases

import com.michaldrabik.showly2.di.scope.AppScope
import com.michaldrabik.showly2.model.SortOrder
import com.michaldrabik.showly2.repository.settings.SettingsRepository
import javax.inject.Inject

@AppScope
class ArchiveSortOrderCase @Inject constructor(
  private val settingsRepository: SettingsRepository
) {

  suspend fun setSortOrder(sortOrder: SortOrder) {
    val settings = settingsRepository.load()
    settingsRepository.update(settings.copy(archiveShowsSortBy = sortOrder))
  }

  suspend fun loadSortOrder(): SortOrder {
    return settingsRepository.load().archiveShowsSortBy
  }
}
