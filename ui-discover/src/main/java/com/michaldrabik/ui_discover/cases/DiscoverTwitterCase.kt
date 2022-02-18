package com.michaldrabik.ui_discover.cases

import com.michaldrabik.repository.settings.SettingsRepository
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class DiscoverTwitterCase @Inject constructor(
  private val settingsRepository: SettingsRepository,
) {

  fun cancelTwitterAd() {
    settingsRepository.isTwitterAdEnabled = false
  }
}
