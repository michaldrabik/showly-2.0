package com.michaldrabik.ui_settings.sections.misc.cases

import com.michaldrabik.repository.settings.SettingsRepository
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class SettingsMiscUserCase @Inject constructor(
  private val settingsRepository: SettingsRepository,
) {

  fun getUserId() = settingsRepository.userId
}
