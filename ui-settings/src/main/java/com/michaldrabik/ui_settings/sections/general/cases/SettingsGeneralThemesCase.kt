package com.michaldrabik.ui_settings.sections.general.cases

import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_settings.helpers.AppTheme
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class SettingsGeneralThemesCase @Inject constructor(
  private val settingsRepository: SettingsRepository,
) {

  fun setTheme(theme: AppTheme) {
    settingsRepository.theme = theme.code
  }

  fun getTheme() = AppTheme.fromCode(settingsRepository.theme)
}
