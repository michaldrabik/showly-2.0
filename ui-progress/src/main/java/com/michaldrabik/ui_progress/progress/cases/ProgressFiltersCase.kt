package com.michaldrabik.ui_progress.progress.cases

import com.michaldrabik.repository.settings.SettingsRepository
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class ProgressFiltersCase @Inject constructor(
  private val settingsRepository: SettingsRepository,
) {

  fun setUpcomingFilter(isEnabled: Boolean) {
    settingsRepository.filters.progressShowsUpcoming = isEnabled
    if (isEnabled) {
      settingsRepository.filters.progressShowsOnHold = false
    }
  }

  fun setOnHoldFilter(isEnabled: Boolean) {
    settingsRepository.filters.progressShowsOnHold = isEnabled
    if (isEnabled) {
      settingsRepository.filters.progressShowsUpcoming = false
    }
  }
}
