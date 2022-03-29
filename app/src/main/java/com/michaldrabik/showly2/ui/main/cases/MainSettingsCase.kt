package com.michaldrabik.showly2.ui.main.cases

import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.showly2.BuildConfig
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class MainSettingsCase @Inject constructor(
  private val settingsRepository: SettingsRepository,
) {

  fun hasMoviesEnabled() = settingsRepository.isMoviesEnabled

  fun hasNewsEnabled(): Boolean {
    if (BuildConfig.DEBUG) return true
    return settingsRepository.isNewsEnabled && settingsRepository.isPremium
  }
}
