package com.michaldrabik.ui_settings.sections.spoilers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.repository.settings.SettingsSpoilersRepository
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_settings.sections.spoilers.helpers.SettingsSpoilersHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsSpoilersViewModel @Inject constructor(
  private val settingsRepository: SettingsSpoilersRepository,
  private val spoilersHelper: SettingsSpoilersHelper
) : ViewModel() {

  private val hasShowsSettingsActive = MutableStateFlow(false)
  private val hasMoviesSettingsActive = MutableStateFlow(false)
  private val hasEpisodesSettingsActive = MutableStateFlow(false)
  private val tapToRevealState = MutableStateFlow(false)

  fun loadSettings() {
    viewModelScope.launch {
      refreshSettings()
    }
  }

  fun setTapToReveal(enable: Boolean) {
    viewModelScope.launch {
      settingsRepository.isTapToReveal = enable
      refreshSettings()
    }
  }

  private fun refreshSettings() {
    with(settingsRepository.getAll()) {
      hasShowsSettingsActive.value = spoilersHelper.hasActiveShowsSettings(this)
      hasMoviesSettingsActive.value = spoilersHelper.hasActiveMoviesSettings(this)
      hasEpisodesSettingsActive.value = spoilersHelper.hasActiveEpisodesSettings(this)
      tapToRevealState.value = isTapToReveal
    }
  }

  val uiState = combine(
    hasShowsSettingsActive,
    hasMoviesSettingsActive,
    hasEpisodesSettingsActive,
    tapToRevealState,
  ) { s1, s2, s3, s4 ->
    SettingsSpoilersUiState(
      hasShowsSettingActive = s1,
      hasMoviesSettingActive = s2,
      hasEpisodesSettingActive = s3,
      isTapToReveal = s4,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = SettingsSpoilersUiState()
  )
}
