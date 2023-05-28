package com.michaldrabik.ui_settings.sections.misc.cases

import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.repository.images.MovieImagesProvider
import com.michaldrabik.repository.images.ShowImagesProvider
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class SettingsMiscCacheCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val showsImagesProvider: ShowImagesProvider,
  private val moviesImagesProvider: MovieImagesProvider,
) {

  suspend fun deleteImagesCache() {
    withContext(dispatchers.IO) {
      showsImagesProvider.deleteLocalCache()
      moviesImagesProvider.deleteLocalCache()
    }
  }
}
