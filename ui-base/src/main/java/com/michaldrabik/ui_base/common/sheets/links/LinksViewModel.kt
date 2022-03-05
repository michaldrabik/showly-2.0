package com.michaldrabik.ui_base.common.sheets.links

import androidx.lifecycle.ViewModel
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.common.AppCountry
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LinksViewModel @Inject constructor(
  private val settingsRepository: SettingsRepository
) : ViewModel() {

  fun loadCountry() = AppCountry.fromCode(settingsRepository.country)
}
