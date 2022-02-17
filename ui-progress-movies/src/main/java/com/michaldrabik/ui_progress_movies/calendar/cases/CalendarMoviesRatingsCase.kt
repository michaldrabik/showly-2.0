package com.michaldrabik.ui_progress_movies.calendar.cases

import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.repository.UserTraktManager
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class CalendarMoviesRatingsCase @Inject constructor(
  private val userTraktManager: UserTraktManager,
  private val settingsRepository: SettingsRepository,
) {

  suspend fun isQuickRateEnabled(): Boolean {
    val isSignedIn = userTraktManager.isAuthorized()
    val isPremium = settingsRepository.isPremium
    val isQuickRate = settingsRepository.load().traktQuickRateEnabled
    return isPremium && isSignedIn && isQuickRate
  }
}
