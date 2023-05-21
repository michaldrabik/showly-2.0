package com.michaldrabik.ui_settings.cases

import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.repository.images.MovieImagesProvider
import com.michaldrabik.repository.images.ShowImagesProvider
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_model.Settings
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class SettingsMainCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val settingsRepository: SettingsRepository,
  private val showsImagesProvider: ShowImagesProvider,
  private val moviesImagesProvider: MovieImagesProvider,
) {

  suspend fun getSettings(): Settings = withContext(dispatchers.IO) {
    settingsRepository.load()
  }

  fun getUserId() = settingsRepository.userId

  fun isPremium() = settingsRepository.isPremium

  suspend fun deleteImagesCache() {
    showsImagesProvider.deleteLocalCache()
    moviesImagesProvider.deleteLocalCache()
  }
}
