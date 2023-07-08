package com.michaldrabik.ui_settings.sections.spoilers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.repository.settings.SettingsSpoilersRepository
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsSpoilersViewModel @Inject constructor(
  private val settingsRepository: SettingsSpoilersRepository
) : ViewModel() {

  private val tapToRevealState = MutableStateFlow(false)

  fun loadSettings() {
    viewModelScope.launch {
      refreshSettings()
    }
  }

  private fun refreshSettings() {
    tapToRevealState.value = settingsRepository.isTapToReveal
  }

  fun setTapToReveal(enable: Boolean) {
    viewModelScope.launch {
      settingsRepository.isTapToReveal = enable
      refreshSettings()
    }
  }

  val uiState =
    tapToRevealState.map {
      SettingsSpoilersUiState(
        isTapToReveal = it
      )
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
      initialValue = SettingsSpoilersUiState()
    )
}
