package com.michaldrabik.ui_settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.michaldrabik.ui_base.Analytics
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_model.MyShowsSection
import com.michaldrabik.ui_model.NotificationDelay
import com.michaldrabik.ui_model.TraktSyncSchedule
import com.michaldrabik.ui_settings.cases.SettingsMainCase
import com.michaldrabik.ui_settings.cases.SettingsTraktCase
import com.michaldrabik.ui_settings.helpers.AppLanguage
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
  private val mainCase: SettingsMainCase,
  private val traktCase: SettingsTraktCase
) : BaseViewModel<SettingsUiModel>() {

  fun loadSettings() {
    viewModelScope.launch {
      refreshSettings()
    }
  }

  fun setRecentShowsAmount(amount: Int) {
    viewModelScope.launch {
      mainCase.setRecentShowsAmount(amount)
      refreshSettings()
      Analytics.logSettingsRecentlyAddedAmount(amount.toLong())
    }
  }

  fun enableQuickSync(enable: Boolean) {
    viewModelScope.launch {
      traktCase.enableTraktQuickSync(enable)
      refreshSettings()
      Analytics.logSettingsTraktQuickSync(enable)
    }
  }

  fun enableQuickRemove(enable: Boolean) {
    viewModelScope.launch {
      traktCase.enableTraktQuickRemove(enable)
      refreshSettings()
      Analytics.logSettingsTraktQuickRemove(enable)
    }
  }

  fun enablePushNotifications(enable: Boolean) {
    viewModelScope.launch {
      mainCase.enablePushNotifications(enable)
      refreshSettings()
      Analytics.logSettingsPushNotifications(enable)
    }
  }

  fun enableAnnouncements(enable: Boolean, context: Context) {
    viewModelScope.launch {
      mainCase.enableAnnouncements(enable, context)
      refreshSettings()
      Analytics.logSettingsAnnouncements(enable)
    }
  }

  fun enableMyShowsSection(section: MyShowsSection, isEnabled: Boolean) {
    viewModelScope.launch {
      mainCase.enableMyShowsSection(section, isEnabled)
      refreshSettings()
      Analytics.logSettingsMyShowsSection(section, isEnabled)
    }
  }

  fun enableArchivedStatistics(enable: Boolean) {
    viewModelScope.launch {
      mainCase.enableArchivedStatistics(enable)
      refreshSettings()
      Analytics.logSettingsArchivedStats(enable)
    }
  }

  fun enableSpecialSeasons(enable: Boolean) {
    viewModelScope.launch {
      mainCase.enableSpecialSeasons(enable)
      refreshSettings()
      Analytics.logSettingsSpecialSeasons(enable)
    }
  }

  fun enableMovies(enable: Boolean, context: Context) {
    viewModelScope.launch {
      mainCase.enableMovies(enable, context)
      delay(250)
      refreshSettings(restartApp = true)
    }
    Analytics.logSettingsMoviesEnabled(enable)
  }

  fun enableWidgetsTitles(enable: Boolean, context: Context) {
    viewModelScope.launch {
      mainCase.enableWidgetsTitles(enable, context)
      refreshSettings()
    }
    Analytics.logSettingsWidgetsTitlesEnabled(enable)
  }

  fun setWhenToNotify(delay: NotificationDelay, context: Context) {
    viewModelScope.launch {
      mainCase.setWhenToNotify(delay, context)
      refreshSettings()
      Analytics.logSettingsWhenToNotify(delay.name)
    }
  }

  fun setLanguage(language: AppLanguage) {
    viewModelScope.launch {
      mainCase.setLanguage(language)
      delay(250)
      refreshSettings(restartApp = true)
    }
    Analytics.logSettingsLanguage(language.code)
  }

  fun setTraktSyncSchedule(schedule: TraktSyncSchedule, context: Context) {
    viewModelScope.launch {
      traktCase.setTraktSyncSchedule(schedule, context)
      refreshSettings()
    }
  }

  fun authorizeTrakt(authData: Uri?) {
    if (authData == null) return
    viewModelScope.launch {
      try {
        traktCase.authorizeTrakt(authData)
        _messageLiveData.value = MessageEvent.info(R.string.textTraktLoginSuccess)
        refreshSettings()
        Analytics.logTraktLogin()
      } catch (t: Throwable) {
        _messageLiveData.value = MessageEvent.error(R.string.errorAuthorization)
        Logger.record(t, "Source" to "${SettingsViewModel::class.simpleName}::authorizeTrakt()")
      }
    }
  }

  fun logoutTrakt(context: Context) {
    viewModelScope.launch {
      traktCase.logoutTrakt(context)
      traktCase.enableTraktQuickSync(false)
      _messageLiveData.value = MessageEvent.info(R.string.textTraktLogoutSuccess)
      refreshSettings()
      Analytics.logTraktLogout()
    }
  }

  fun deleteImagesCache(context: Context) {
    viewModelScope.launch {
      withContext(IO) { Glide.get(context).clearDiskCache() }
      Glide.get(context).clearMemory()
      mainCase.deleteImagesCache()
      _messageLiveData.value = MessageEvent.info(R.string.textImagesCacheCleared)
    }
  }

  private suspend fun refreshSettings(restartApp: Boolean = false) {
    uiState = SettingsUiModel(
      settings = mainCase.getSettings(),
      language = mainCase.getLanguage(),
      moviesEnabled = mainCase.isMoviesEnabled(),
      isSignedInTrakt = traktCase.isTraktAuthorized(),
      traktUsername = traktCase.getTraktUsername(),
      restartApp = restartApp
    )
  }
}
