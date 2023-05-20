package com.michaldrabik.ui_settings

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.michaldrabik.ui_base.Analytics
import com.michaldrabik.ui_base.common.AppCountry
import com.michaldrabik.ui_base.dates.AppDateFormat
import com.michaldrabik.ui_base.utilities.events.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.utilities.extensions.combine
import com.michaldrabik.ui_base.viewmodel.ChannelsDelegate
import com.michaldrabik.ui_base.viewmodel.DefaultChannelsDelegate
import com.michaldrabik.ui_model.MyMoviesSection
import com.michaldrabik.ui_model.MyShowsSection
import com.michaldrabik.ui_model.ProgressNextEpisodeType
import com.michaldrabik.ui_model.Settings
import com.michaldrabik.ui_settings.cases.SettingsMainCase
import com.michaldrabik.ui_settings.cases.SettingsStreamingsCase
import com.michaldrabik.ui_settings.cases.SettingsThemesCase
import com.michaldrabik.ui_settings.helpers.AppLanguage
import com.michaldrabik.ui_settings.helpers.AppTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

// TODO Settings are getting too big. Refactor into smaller parts.
@HiltViewModel
class SettingsViewModel @Inject constructor(
  private val mainCase: SettingsMainCase,
  private val themesCase: SettingsThemesCase,
  private val streamingsCase: SettingsStreamingsCase,
) : ViewModel(), ChannelsDelegate by DefaultChannelsDelegate() {

  private val settingsState = MutableStateFlow<Settings?>(null)
  private val languageState = MutableStateFlow(AppLanguage.ENGLISH)
  private val themeState = MutableStateFlow(AppTheme.DARK)
  private val countryState = MutableStateFlow<AppCountry?>(null)
  private val dateFormatState = MutableStateFlow<AppDateFormat?>(null)
  private val moviesEnabledState = MutableStateFlow(true)
  private val newsEnabledState = MutableStateFlow(false)
  private val streamingsEnabledState = MutableStateFlow(true)
  private val premiumState = MutableStateFlow(false)
  private val restartAppState = MutableStateFlow(false)
  private val progressTypeState = MutableStateFlow<ProgressNextEpisodeType?>(null)

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

  fun setLanguage(language: AppLanguage) {
    viewModelScope.launch {
      mainCase.setLanguage(language)
      val locales = LocaleListCompat.forLanguageTags(language.code)
      AppCompatDelegate.setApplicationLocales(locales)
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

  fun setCountry(country: AppCountry) {
    viewModelScope.launch {
      mainCase.setCountry(country)
      streamingsCase.deleteCache()
      refreshSettings()
    }
    Analytics.logSettingsCountry(country.code)
  }

  fun setProgressType(type: ProgressNextEpisodeType) {
    viewModelScope.launch {
      mainCase.setProgressType(type)
      refreshSettings()
    }
    Analytics.logSettingsProgressType(type.name)
  }

  fun setDateFormat(format: AppDateFormat, context: Context) {
    viewModelScope.launch {
      mainCase.setDateFormat(format, context)
      refreshSettings()
    }
    Analytics.logSettingsDateFormat(format.name)
  }

  fun deleteImagesCache(context: Context) {
    viewModelScope.launch {
      withContext(IO) { Glide.get(context).clearDiskCache() }
      Glide.get(context).clearMemory()
      mainCase.deleteImagesCache()
      messageChannel.send(MessageEvent.Info(R.string.textImagesCacheCleared))
    }
  }

  private suspend fun refreshSettings(restartApp: Boolean = false) {
    settingsState.value = mainCase.getSettings()
    languageState.value = mainCase.getLanguage()
    themeState.value = themesCase.getTheme()
    countryState.value = mainCase.getCountry()
    dateFormatState.value = mainCase.getDateFormat()
    moviesEnabledState.value = mainCase.isMoviesEnabled()
    newsEnabledState.value = mainCase.isNewsEnabled()
    streamingsEnabledState.value = mainCase.isStreamingsEnabled()
//    signedInTraktState.value = traktCase.isTraktAuthorized()
    premiumState.value = mainCase.isPremium()
//    traktNameState.value = traktCase.getTraktUsername()
//    userIdState.value = mainCase.getUserId()
    restartAppState.value = restartApp
    progressTypeState.value = mainCase.getProgressType()
  }

  val uiState = combine(
    settingsState,
    languageState,
    themeState,
    countryState,
    dateFormatState,
    moviesEnabledState,
    newsEnabledState,
    streamingsEnabledState,
    premiumState,
    restartAppState,
    progressTypeState
  ) { s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11 ->
    SettingsUiState(
      settings = s1,
      language = s2,
      theme = s3,
      country = s4,
      dateFormat = s5,
      moviesEnabled = s6,
      newsEnabled = s7,
      streamingsEnabled = s8,
      isPremium = s9,
      restartApp = s10,
      progressNextType = s11
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = SettingsUiState()
  )
}
