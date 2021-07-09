package com.michaldrabik.ui_settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.michaldrabik.ui_base.Analytics
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_base.common.AppCountry
import com.michaldrabik.ui_base.dates.AppDateFormat
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_model.MyMoviesSection
import com.michaldrabik.ui_model.MyShowsSection
import com.michaldrabik.ui_model.NotificationDelay
import com.michaldrabik.ui_model.TraktSyncSchedule
import com.michaldrabik.ui_settings.cases.SettingsMainCase
import com.michaldrabik.ui_settings.cases.SettingsStreamingsCase
import com.michaldrabik.ui_settings.cases.SettingsThemesCase
import com.michaldrabik.ui_settings.cases.SettingsTraktCase
import com.michaldrabik.ui_settings.helpers.AppLanguage
import com.michaldrabik.ui_settings.helpers.AppTheme
import com.michaldrabik.ui_settings.helpers.WidgetTransparency
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
  private val mainCase: SettingsMainCase,
  private val traktCase: SettingsTraktCase,
  private val themesCase: SettingsThemesCase,
  private val streamingsCase: SettingsStreamingsCase,
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

  fun enableQuickRate(enable: Boolean) {
    viewModelScope.launch {
      traktCase.enableTraktQuickRate(enable)
      refreshSettings()
      Analytics.logSettingsTraktQuickRate(enable)
    }
  }

  fun enablePushNotifications(enable: Boolean) {
    viewModelScope.launch {
      mainCase.enablePushNotifications(enable)
      refreshSettings()
      Analytics.logSettingsPushNotifications(enable)
    }
  }

  fun enableAnnouncements(enable: Boolean) {
    viewModelScope.launch {
      mainCase.enableAnnouncements(enable)
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

  fun enableMyMoviesSection(section: MyMoviesSection, isEnabled: Boolean) {
    viewModelScope.launch {
      mainCase.enableMyMoviesSection(section, isEnabled)
      refreshSettings()
      Analytics.logSettingsMyMoviesSection(section, isEnabled)
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

  fun enableProgressUpcoming(enable: Boolean, context: Context) {
    viewModelScope.launch {
      mainCase.enableProgressUpcoming(enable, context)
      refreshSettings()
      Analytics.logSettingsProgressUpcoming(enable)
    }
  }

  fun enableMovies(enable: Boolean) {
    viewModelScope.launch {
      mainCase.enableMovies(enable)
      delay(300)
      refreshSettings(restartApp = true)
    }
    Analytics.logSettingsMoviesEnabled(enable)
  }

  fun enableNews(enable: Boolean) {
    viewModelScope.launch {
      mainCase.enableNews(enable)
      delay(300)
      refreshSettings(restartApp = true)
    }
    Analytics.logSettingsNewsEnabled(enable)
  }

  fun enableStreamings(enable: Boolean) {
    viewModelScope.launch {
      mainCase.enableStreamings(enable)
      refreshSettings()
    }
    Analytics.logSettingsStreamingsEnabled(enable)
  }

  fun enableWidgetsTitles(enable: Boolean, context: Context) {
    viewModelScope.launch {
      mainCase.enableWidgetsTitles(enable, context)
      refreshSettings()
    }
    Analytics.logSettingsWidgetsTitlesEnabled(enable)
  }

  fun setWhenToNotify(delay: NotificationDelay) {
    viewModelScope.launch {
      mainCase.setWhenToNotify(delay)
      refreshSettings()
      Analytics.logSettingsWhenToNotify(delay.name)
    }
  }

  fun setLanguage(language: AppLanguage) {
    viewModelScope.launch {
      mainCase.setLanguage(language)
      delay(300)
      refreshSettings(restartApp = true)
    }
    Analytics.logSettingsLanguage(language.code)
  }

  fun setTheme(theme: AppTheme) {
    viewModelScope.launch {
      themesCase.setTheme(theme)
      refreshSettings()
    }
    Analytics.logSettingsTheme(theme.code)
  }

  fun setWidgetsTheme(theme: AppTheme, context: Context) {
    viewModelScope.launch {
      themesCase.setWidgetsTheme(theme, context)
      refreshSettings()
    }
    Analytics.logSettingsWidgetsTheme(theme.code)
  }

  fun setWidgetsTransparency(transparency: WidgetTransparency, context: Context) {
    viewModelScope.launch {
      themesCase.setWidgetsTransparency(transparency, context)
      refreshSettings()
    }
  }

  fun setCountry(country: AppCountry) {
    viewModelScope.launch {
      mainCase.setCountry(country)
      streamingsCase.deleteCache()
      refreshSettings()
    }
    Analytics.logSettingsCountry(country.code)
  }

  fun setDateFormat(format: AppDateFormat, context: Context) {
    viewModelScope.launch {
      mainCase.setDateFormat(format, context)
      refreshSettings()
    }
    Analytics.logSettingsDateFormat(format.name)
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
        uiState = SettingsUiModel(isSigningIn = true)
        traktCase.authorizeTrakt(authData)
        _messageLiveData.value = MessageEvent.info(R.string.textTraktLoginSuccess)
        refreshSettings()
        Analytics.logTraktLogin()
      } catch (error: Throwable) {
        val message = when {
          error is HttpException && error.code() == 423 -> R.string.errorTraktLocked
          else -> R.string.errorAuthorization
        }
        _messageLiveData.value = MessageEvent.error(message)
        Logger.record(error, "Source" to "SettingsViewModel::authorizeTrakt()")
      } finally {
        uiState = SettingsUiModel(isSigningIn = false)
      }
    }
  }

  fun logoutTrakt(context: Context) {
    viewModelScope.launch {
      traktCase.logoutTrakt(context)
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
      theme = themesCase.getTheme(),
      themeWidgets = themesCase.getWidgetsTheme(),
      widgetsTransparency = themesCase.getWidgetsTransparency(),
      country = mainCase.getCountry(),
      dateFormat = mainCase.getDateFormat(),
      moviesEnabled = mainCase.isMoviesEnabled(),
      newsEnabled = mainCase.isNewsEnabled(),
      streamingsEnabled = mainCase.isStreamingsEnabled(),
      isSignedInTrakt = traktCase.isTraktAuthorized(),
      isPremium = mainCase.isPremium(),
      traktUsername = traktCase.getTraktUsername(),
      userId = mainCase.getUserId(),
      restartApp = restartApp
    )
  }
}
