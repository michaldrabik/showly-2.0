package com.michaldrabik.ui_settings.cases

import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.repository.movies.MovieStreamingsRepository
import com.michaldrabik.repository.shows.ShowStreamingsRepository
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class SettingsStreamingsCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val showStreamingsRepository: ShowStreamingsRepository,
  private val movieStreamingsRepository: MovieStreamingsRepository,
) {

  suspend fun deleteCache() = withContext(dispatchers.IO) {
    showStreamingsRepository.deleteCache()
    movieStreamingsRepository.deleteCache()
  }
}
