package com.michaldrabik.ui_settings.sections.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.Analytics
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_model.NotificationDelay
import com.michaldrabik.ui_model.Settings
import com.michaldrabik.ui_settings.sections.notifications.cases.SettingsNotificationsMainCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsNotificationsViewModel @Inject constructor(
  private val mainCase: SettingsNotificationsMainCase,
) : ViewModel() {

  private val settingsState = MutableStateFlow<Settings?>(null)
  private val loadingState = MutableStateFlow(false)

  fun loadSettings() {
    viewModelScope.launch {
      refreshSettings()
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

  fun setWhenToNotify(delay: NotificationDelay) {
    viewModelScope.launch {
      mainCase.setWhenToNotify(delay)
      refreshSettings()
      Analytics.logSettingsWhenToNotify(delay.name)
    }
  }

  private suspend fun refreshSettings() {
    settingsState.value = mainCase.getSettings()
  }

  val uiState = combine(
    settingsState,
    loadingState
  ) { s1, _ ->
    SettingsNotificationsUiState(
      settings = s1,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = SettingsNotificationsUiState()
  )
}
