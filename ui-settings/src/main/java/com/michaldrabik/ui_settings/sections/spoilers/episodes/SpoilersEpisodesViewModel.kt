package com.michaldrabik.ui_settings.sections.spoilers.episodes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.repository.settings.SettingsSpoilersRepository
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SpoilersEpisodesViewModel @Inject constructor(
  private val settingsRepository: SettingsSpoilersRepository
) : ViewModel() {

  private val initialState = SpoilersEpisodesUiState()
  private val settingsState = MutableStateFlow(initialState.settings)

  fun refreshSettings() {
    settingsState.update { settingsRepository.getAll() }
  }

  fun setHideTitle(hide: Boolean) {
    settingsRepository.isEpisodesTitleHidden = hide
    refreshSettings()
  }

  fun setHideDescription(hide: Boolean) {
    settingsRepository.isEpisodesDescriptionHidden = hide
    refreshSettings()
  }

  fun setHideRating(hide: Boolean) {
    settingsRepository.isEpisodesRatingHidden = hide
    refreshSettings()
  }

  fun setHideImage(hide: Boolean) {
    settingsRepository.isEpisodesImageHidden = hide
    refreshSettings()
  }

  val uiState = settingsState
    .map { SpoilersEpisodesUiState(it) }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
      initialValue = SpoilersEpisodesUiState()
    )
}
