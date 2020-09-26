package com.michaldrabik.showly2.ui.settings.cases

import android.content.Context
import android.net.Uri
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.showly2.common.trakt.TraktSyncWorker
import com.michaldrabik.ui_model.TraktSyncSchedule
import com.michaldrabik.ui_repository.RatingsRepository
import com.michaldrabik.ui_repository.SettingsRepository
import com.michaldrabik.ui_repository.UserTraktManager
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
