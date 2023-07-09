package com.michaldrabik.ui_settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.viewmodel.ChannelsDelegate
import com.michaldrabik.ui_base.viewmodel.DefaultChannelsDelegate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
  private val settingsRepository: SettingsRepository,
) : ViewModel(), ChannelsDelegate by DefaultChannelsDelegate() {

  private val premiumState = MutableStateFlow(false)

  fun loadSettings() {
    viewModelScope.launch {
      premiumState.value = settingsRepository.isPremium
    }
  }

  val uiState = premiumState
    .map { SettingsUiState(it) }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
      initialValue = SettingsUiState()
    )
}
