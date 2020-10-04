package com.michaldrabik.ui_my_shows.archive.cases

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_repository.SettingsRepository
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
