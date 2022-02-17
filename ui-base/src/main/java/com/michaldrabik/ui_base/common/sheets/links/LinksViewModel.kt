package com.michaldrabik.ui_base.common.sheets.links

import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.common.AppCountry
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LinksViewModel @Inject constructor(
  private val settingsRepository: SettingsRepository
) : BaseViewModel() {

  fun loadCountry() = AppCountry.fromCode(settingsRepository.country)
}
