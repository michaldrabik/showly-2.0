package com.michaldrabik.ui_settings.sections.widgets

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.Analytics
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_model.Settings
import com.michaldrabik.ui_settings.helpers.AppTheme
import com.michaldrabik.ui_settings.helpers.WidgetTransparency
import com.michaldrabik.ui_settings.sections.widgets.cases.SettingsWidgetsMainCase
import com.michaldrabik.ui_settings.sections.widgets.cases.SettingsWidgetsThemesCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsWidgetsViewModel @Inject constructor(
  private val mainCase: SettingsWidgetsMainCase,
  private val themesCase: SettingsWidgetsThemesCase,
) : ViewModel() {

  private val settingsState = MutableStateFlow<Settings?>(null)

  private val widgetThemeState = MutableStateFlow(AppTheme.DARK)
  private val widgetTransparencyState = MutableStateFlow(WidgetTransparency.SOLID)
  private val premiumState = MutableStateFlow(false)

  fun loadSettings() {
    viewModelScope.launch {
      refreshSettings()
    }
  }

  fun enableWidgetsTitles(enable: Boolean, context: Context) {
    viewModelScope.launch {
      mainCase.enableWidgetsTitles(enable, context)
      refreshSettings()
    }
    Analytics.logSettingsWidgetsTitlesEnabled(enable)
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

  private suspend fun refreshSettings() {
    settingsState.value = mainCase.getSettings()
    widgetThemeState.value = themesCase.getWidgetsTheme()
    widgetTransparencyState.value = themesCase.getWidgetsTransparency()
    premiumState.value = mainCase.isPremium()
  }

  val uiState = combine(
    settingsState,
    premiumState,
    widgetThemeState,
    widgetTransparencyState
  ) { s1, s2, s3, s4 ->
    SettingsWidgetsUiState(
      settings = s1,
      isPremium = s2,
      themeWidgets = s3,
      widgetsTransparency = s4,

    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = SettingsWidgetsUiState()
  )
}
