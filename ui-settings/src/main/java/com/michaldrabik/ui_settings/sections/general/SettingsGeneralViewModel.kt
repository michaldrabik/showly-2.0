package com.michaldrabik.ui_settings.sections.general

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.Config
import com.michaldrabik.ui_model.locale.AppCountry
import com.michaldrabik.ui_base.dates.AppDateFormat
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.utilities.extensions.combine
import com.michaldrabik.ui_model.ProgressDateSelectionType
import com.michaldrabik.ui_model.ProgressNextEpisodeType
import com.michaldrabik.ui_model.Settings
import com.michaldrabik.ui_model.locale.AppLanguage
import com.michaldrabik.ui_settings.sections.general.cases.SettingsGeneralMainCase
import com.michaldrabik.ui_settings.sections.general.cases.SettingsGeneralStreamingsCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsGeneralViewModel @Inject constructor(
  private val mainCase: SettingsGeneralMainCase,
  private val streamingsCase: SettingsGeneralStreamingsCase,
) : ViewModel() {

  private val settingsState = MutableStateFlow<Settings?>(null)
  private val languageState = MutableStateFlow(AppLanguage.ENGLISH)
  private val countryState = MutableStateFlow<AppCountry?>(null)
  private val dateFormatState = MutableStateFlow<AppDateFormat?>(null)
  private val moviesEnabledState = MutableStateFlow(true)
  private val streamingsEnabledState = MutableStateFlow(true)
  private val premiumState = MutableStateFlow(false)
  private val restartAppState = MutableStateFlow(false)
  private val progressTypeState = MutableStateFlow<ProgressNextEpisodeType?>(null)
  private val progressDateSelectionState = MutableStateFlow<ProgressDateSelectionType?>(null)
  private val progressUpcomingDaysState = MutableStateFlow<Long?>(null)
  private val tabletsColumnsState = MutableStateFlow(Config.DEFAULT_LISTS_GRID_SPAN)

  fun loadSettings() {
    viewModelScope.launch {
      refreshSettings()
    }
  }

  private suspend fun refreshSettings(restartApp: Boolean = false) {
    settingsState.value = mainCase.getSettings()
    languageState.value = mainCase.getLanguage()
    countryState.value = mainCase.getCountry()
    dateFormatState.value = mainCase.getDateFormat()
    moviesEnabledState.value = mainCase.isMoviesEnabled()
    streamingsEnabledState.value = mainCase.isStreamingsEnabled()
    progressTypeState.value = mainCase.getProgressType()
    progressDateSelectionState.value = mainCase.getDateSelectionType()
    progressUpcomingDaysState.value = mainCase.getProgressUpcomingDays()
    tabletsColumnsState.value = mainCase.getTabletsColumns()
    restartAppState.value = restartApp
  }

  fun setRecentShowsAmount(amount: Int) {
    viewModelScope.launch {
      mainCase.setRecentShowsAmount(amount)
      refreshSettings()
    }
  }

  fun enableSpecialSeasons(enable: Boolean) {
    viewModelScope.launch {
      mainCase.enableSpecialSeasons(enable)
      refreshSettings()
    }
  }

  fun enableMovies(enable: Boolean) {
    viewModelScope.launch {
      mainCase.enableMovies(enable)
      delay(300)
      refreshSettings(restartApp = true)
    }
  }

  fun enableStreamings(enable: Boolean) {
    viewModelScope.launch {
      mainCase.enableStreamings(enable)
      refreshSettings()
    }
  }

  fun setLanguage(language: AppLanguage) {
    viewModelScope.launch {
      mainCase.setLanguage(language)
      val locales = LocaleListCompat.forLanguageTags(language.code)
      AppCompatDelegate.setApplicationLocales(locales)
    }
  }

  fun setTabletColumns(columns: Int) {
    viewModelScope.launch {
      mainCase.setTabletsColumns(columns)
      refreshSettings()
    }
  }

  fun setCountry(country: AppCountry) {
    viewModelScope.launch {
      mainCase.setCountry(country)
      streamingsCase.deleteCache()
      refreshSettings()
    }
  }

  fun setProgressType(type: ProgressNextEpisodeType) {
    viewModelScope.launch {
      mainCase.setProgressType(type)
      refreshSettings()
    }
  }

  fun setDateSelectionType(type: ProgressDateSelectionType) {
    viewModelScope.launch {
      mainCase.setDateSelectionType(type)
      refreshSettings()
    }
  }

  fun setProgressUpcomingDays(days: Long) {
    viewModelScope.launch {
      mainCase.setProgressUpcomingDays(days)
      refreshSettings()
    }
  }

  fun setDateFormat(format: AppDateFormat, context: Context) {
    viewModelScope.launch {
      mainCase.setDateFormat(format, context)
      refreshSettings()
    }
  }

  val uiState = combine(
    settingsState,
    premiumState,
    languageState,
    countryState,
    dateFormatState,
    moviesEnabledState,
    streamingsEnabledState,
    progressTypeState,
    restartAppState,
    progressUpcomingDaysState,
    tabletsColumnsState,
    progressDateSelectionState
  ) { s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12 ->
    SettingsGeneralUiState(
      settings = s1,
      isPremium = s2,
      language = s3,
      country = s4,
      dateFormat = s5,
      moviesEnabled = s6,
      streamingsEnabled = s7,
      progressNextType = s8,
      restartApp = s9,
      progressUpcomingDays = s10,
      tabletColumns = s11,
      progressDateSelectionType = s12
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = SettingsGeneralUiState()
  )
}
