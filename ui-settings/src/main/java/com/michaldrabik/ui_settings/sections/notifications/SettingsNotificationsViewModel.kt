package com.michaldrabik.ui_settings.sections.notifications

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.Analytics
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.viewmodel.ChannelsDelegate
import com.michaldrabik.ui_base.viewmodel.DefaultChannelsDelegate
import com.michaldrabik.ui_model.NotificationDelay
import com.michaldrabik.ui_model.Settings
import com.michaldrabik.ui_settings.sections.notifications.SettingsNotificationsUiEvent.RequestNotificationsPermission
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
) : ViewModel(), ChannelsDelegate by DefaultChannelsDelegate() {

  private val settingsState = MutableStateFlow<Settings?>(null)
  private val loadingState = MutableStateFlow(false)

  fun loadSettings(context: Context) {
    viewModelScope.launch {
      ensureNotificationsPermission(context)
      refreshSettings()
    }
  }

  fun enableNotifications(enable: Boolean, context: Context) {
    viewModelScope.launch {
      if (enable && !ensureNotificationsPermission(context)) {
        eventChannel.send(RequestNotificationsPermission)
        return@launch
      }
      mainCase.enableNotifications(enable)
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

  private suspend fun ensureNotificationsPermission(context: Context): Boolean {
    val areNotificationsEnabled = NotificationManagerCompat
      .from(context.applicationContext)
      .areNotificationsEnabled()

    if (!areNotificationsEnabled) {
      mainCase.enableNotifications(false)
    }

    return areNotificationsEnabled
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
