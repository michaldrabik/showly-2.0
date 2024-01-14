package com.michaldrabik.ui_progress.progress.cases

import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_progress.progress.recycler.ProgressListItem.Header.Type
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class ProgressHeadersCase @Inject constructor(
  private val settingsRepository: SettingsRepository,
) {

  fun toggleHeaderCollapsed(type: Type) {
    when (type) {
      Type.UPCOMING -> {
        val isCollapsed = settingsRepository.isProgressUpcomingCollapsed
        settingsRepository.isProgressUpcomingCollapsed = !isCollapsed
      }
      Type.ON_HOLD -> {
        val isCollapsed = settingsRepository.isProgressOnHoldCollapsed
        settingsRepository.isProgressOnHoldCollapsed = !isCollapsed
      }
    }
  }
}
