package com.michaldrabik.ui_progress.calendar.cases

import com.michaldrabik.repository.RatingsRepository
import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.ui_model.Episode
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class CalendarRatingsCase @Inject constructor(
  private val userTraktManager: UserTraktManager,
  private val ratingsRepository: RatingsRepository,
  private val settingsRepository: SettingsRepository,
) {

  suspend fun isQuickRateEnabled(): Boolean {
    val isSignedIn = userTraktManager.isAuthorized()
    val isPremium = settingsRepository.isPremium
    val isQuickRate = settingsRepository.load().traktQuickRateEnabled
    return isPremium && isSignedIn && isQuickRate
  }

  suspend fun addRating(episode: Episode, rating: Int) {
    val token = userTraktManager.checkAuthorization().token
    ratingsRepository.shows.addRating(token, episode, rating)
  }
}
