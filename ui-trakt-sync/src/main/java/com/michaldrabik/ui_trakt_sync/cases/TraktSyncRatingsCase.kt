package com.michaldrabik.ui_trakt_sync.cases

import com.michaldrabik.repository.RatingsRepository
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.repository.settings.SettingsRepository
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class TraktSyncRatingsCase @Inject constructor(
  private val userTraktManager: UserTraktManager,
  private val settingsRepository: SettingsRepository,
  private val ratingsRepository: RatingsRepository,
) {

  suspend fun preloadRatings() {
    if (userTraktManager.isAuthorized()) {
      val token = userTraktManager.checkAuthorization().token
      with(ratingsRepository) {
        shows.preloadRatings(token)
        if (settingsRepository.isMoviesEnabled) {
          movies.preloadRatings(token)
        }
      }
    }
  }
}
