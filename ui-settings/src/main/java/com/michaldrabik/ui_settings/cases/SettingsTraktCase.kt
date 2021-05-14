package com.michaldrabik.ui_settings.cases

import android.content.Context
import android.net.Uri
import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.repository.ratings.RatingsRepository
import com.michaldrabik.ui_base.trakt.TraktSyncWorker
import com.michaldrabik.ui_model.TraktSyncSchedule
import javax.inject.Inject

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

  suspend fun enableTraktQuickRate(enable: Boolean) {
    val settings = settingsRepository.load()
    settings.let {
      val new = it.copy(traktQuickRateEnabled = enable)
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

    suspend fun disableTraktFeatures(context: Context) {
      val settings = settingsRepository.load()
      settings.let {
        val new = it.copy(
          traktQuickSyncEnabled = false,
          traktQuickRemoveEnabled = false,
          traktQuickRateEnabled = false
        )
        settingsRepository.update(new)
      }
      setTraktSyncSchedule(TraktSyncSchedule.OFF, context)
    }

    userManager.revokeToken()
    userManager.clearTraktLogs()
    ratingsRepository.clear()
    disableTraktFeatures(context)
    TraktSyncWorker.cancelAll(context)
  }

  suspend fun isTraktAuthorized() = userManager.isAuthorized()

  suspend fun getTraktUsername() = userManager.getUsername()
}
