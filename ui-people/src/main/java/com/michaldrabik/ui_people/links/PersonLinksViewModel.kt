package com.michaldrabik.ui_people.links

import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.common.AppCountry
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PersonLinksViewModel @Inject constructor(
  private val settingsRepository: SettingsRepository
) : BaseViewModel() {

  fun loadCountry() = AppCountry.fromCode(settingsRepository.country)
}
