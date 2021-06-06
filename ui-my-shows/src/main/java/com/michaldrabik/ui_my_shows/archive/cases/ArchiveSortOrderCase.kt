package com.michaldrabik.ui_my_shows.archive.cases

import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.ui_model.SortOrder
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class ArchiveSortOrderCase @Inject constructor(
  private val settingsRepository: SettingsRepository,
) {

  suspend fun setSortOrder(sortOrder: SortOrder) {
    val settings = settingsRepository.load()
    settingsRepository.update(settings.copy(archiveShowsSortBy = sortOrder))
  }

  suspend fun loadSortOrder(): SortOrder {
    return settingsRepository.load().archiveShowsSortBy
  }
}
