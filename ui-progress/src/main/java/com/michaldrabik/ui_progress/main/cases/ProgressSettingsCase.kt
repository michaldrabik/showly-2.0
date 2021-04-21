package com.michaldrabik.ui_progress.main.cases

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.repository.SettingsRepository
import javax.inject.Inject

@AppScope
class ProgressSettingsCase @Inject constructor(
  private val settingsRepository: SettingsRepository,
) {

  suspend fun isUpcomingEnabled() = settingsRepository.load().progressUpcomingEnabled
}
