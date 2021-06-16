package com.michaldrabik.ui_progress.main.cases

import com.michaldrabik.repository.SettingsRepository
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class ProgressMainSettingsCase @Inject constructor(
  private val settingsRepository: SettingsRepository,
) {

  suspend fun isUpcomingEnabled() = settingsRepository.load().progressUpcomingEnabled

  fun getProgressType() = settingsRepository.progressPercentType
}
