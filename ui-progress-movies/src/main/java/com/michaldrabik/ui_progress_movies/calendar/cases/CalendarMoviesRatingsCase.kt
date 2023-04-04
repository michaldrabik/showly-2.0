package com.michaldrabik.ui_progress_movies.calendar.cases

import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.repository.settings.SettingsRepository
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class CalendarMoviesRatingsCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val userTraktManager: UserTraktManager,
  private val settingsRepository: SettingsRepository,
) {

  suspend fun isQuickRateEnabled(): Boolean =
    withContext(dispatchers.IO) {
      val isSignedIn = userTraktManager.isAuthorized()
      val isPremium = settingsRepository.isPremium
      val isQuickRate = settingsRepository.load().traktQuickRateEnabled
      isPremium && isSignedIn && isQuickRate
    }
}
