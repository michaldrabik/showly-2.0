package com.michaldrabik.ui_show.cases

import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.repository.StreamingsRepository
import com.michaldrabik.ui_base.common.AppCountry
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.StreamingService
import javax.inject.Inject

class ShowDetailsStreamingCase @Inject constructor(
  private val streamingsRepository: StreamingsRepository,
  private val settingsRepository: SettingsRepository,
) {

  suspend fun loadStreamingServices(show: Show): List<StreamingService> {
    val country = AppCountry.fromCode(settingsRepository.country)
    return streamingsRepository.loadStreamings(show, country.code)
  }
}
