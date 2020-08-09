package com.michaldrabik.showly2.ui.followedshows.seelater.cases

import com.michaldrabik.showly2.di.scope.AppScope
import com.michaldrabik.showly2.model.SortOrder
import com.michaldrabik.showly2.repository.settings.SettingsRepository
import javax.inject.Inject

@AppScope
class SeeLaterSortOrderCase @Inject constructor(
  private val settingsRepository: SettingsRepository
) {

  suspend fun setSortOrder(sortOrder: SortOrder) {
    val settings = settingsRepository.load()
    settingsRepository.update(settings.copy(seeLaterShowsSortBy = sortOrder))
  }

  suspend fun loadSortOrder(): SortOrder {
    return settingsRepository.load().seeLaterShowsSortBy
  }
}
