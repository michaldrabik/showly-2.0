package com.michaldrabik.ui_settings.cases

import com.michaldrabik.repository.RatingsRepository
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.repository.settings.SettingsRepository
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class SettingsRatingsCase @Inject constructor(
  private val userTraktManager: UserTraktManager,
  private val settingsRepository: SettingsRepository,
  private val ratingsRepository: RatingsRepository,
) {

  suspend fun preloadRatings() {
    if (userTraktManager.isAuthorized()) {
      userTraktManager.checkAuthorization()
      with(ratingsRepository) {
        shows.preloadRatings()
        if (settingsRepository.isMoviesEnabled) {
          movies.preloadRatings()
        }
      }
    }
  }
}
