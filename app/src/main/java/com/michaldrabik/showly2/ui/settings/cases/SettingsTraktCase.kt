package com.michaldrabik.showly2.ui.settings.cases

import android.content.Context
import android.net.Uri
import com.michaldrabik.showly2.common.trakt.TraktSyncWorker
import com.michaldrabik.showly2.di.scope.AppScope
import com.michaldrabik.showly2.model.TraktSyncSchedule
import com.michaldrabik.showly2.repository.UserTraktManager
import com.michaldrabik.showly2.repository.rating.RatingsRepository
import com.michaldrabik.showly2.repository.settings.SettingsRepository
import javax.inject.Inject

@AppScope
class SettingsTraktCase @Inject constructor(
  private val settingsRepository: SettingsRepository,
  private val ratingsRepository: RatingsRepository,
  private val userManager: UserTraktManager
) {

  suspend fun enableTraktQuickSync(enable: Boolean) {
    val settings = settingsRepository.load()
    settings.let {
      val new = it.copy(traktQuickSyncEnabled = enable)
      settingsRepository.update(new)
    }
  }

  suspend fun enableTraktQuickRemove(enable: Boolean) {
    val settings = settingsRepository.load()
    settings.let {
      val new = it.copy(traktQuickRemoveEnabled = enable)
      settingsRepository.update(new)
    }
  }

  suspend fun setTraktSyncSchedule(schedule: TraktSyncSchedule, context: Context) {
    val settings = settingsRepository.load()
    settings.let {
      val new = it.copy(traktSyncSchedule = schedule)
      settingsRepository.update(new)
    }
    TraktSyncWorker.schedule(schedule, context.applicationContext)
  }

  suspend fun authorizeTrakt(authData: Uri) {
    val code = authData.getQueryParameter("code")
    if (code.isNullOrBlank()) {
      throw IllegalStateException("Invalid Trakt authorization code.")
    }
    userManager.authorize(code)
  }

  suspend fun logoutTrakt(context: Context) {
    userManager.revokeToken()
    ratingsRepository.clear()
    TraktSyncWorker.cancelAll(context)
  }

  suspend fun isTraktAuthorized() = userManager.isAuthorized()

  suspend fun getTraktUsername() = userManager.getTraktUsername()
}
