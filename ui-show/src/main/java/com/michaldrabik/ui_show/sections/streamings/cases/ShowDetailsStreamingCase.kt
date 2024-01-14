package com.michaldrabik.ui_show.sections.streamings.cases

import com.michaldrabik.common.ConfigVariant
import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.common.extensions.nowUtc
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.repository.shows.ShowStreamingsRepository
import com.michaldrabik.ui_base.common.AppCountry
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.StreamingService
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class ShowDetailsStreamingCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val streamingsRepository: ShowStreamingsRepository,
  private val settingsRepository: SettingsRepository,
) {

  suspend fun getLocalStreamingServices(show: Show): List<StreamingService> =
    withContext(dispatchers.IO) {
      if (!settingsRepository.streamingsEnabled) {
        return@withContext emptyList()
      }
      val country = AppCountry.fromCode(settingsRepository.country)
      val localData = streamingsRepository.getLocalStreamings(show, country.code)
      return@withContext localData.first
    }

  suspend fun loadStreamingServices(show: Show): List<StreamingService> =
    withContext(dispatchers.IO) {
      if (!settingsRepository.streamingsEnabled) {
        return@withContext emptyList()
      }
      val country = AppCountry.fromCode(settingsRepository.country)
      val (localItems, timestamp) = streamingsRepository.getLocalStreamings(show, country.code)
      if (timestamp != null && timestamp.plusSeconds(ConfigVariant.STREAMINGS_CACHE_DURATION / 1000).isAfter(nowUtc())) {
        return@withContext localItems
      }
      streamingsRepository.loadRemoteStreamings(show, country.code)
    }
}
