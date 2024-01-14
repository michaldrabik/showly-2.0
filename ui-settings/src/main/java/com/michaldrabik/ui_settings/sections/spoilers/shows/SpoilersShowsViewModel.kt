package com.michaldrabik.ui_settings.sections.spoilers.shows

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
class SpoilersShowsViewModel @Inject constructor(
  private val settingsRepository: SettingsSpoilersRepository
) : ViewModel() {

  private val initialState = SpoilersShowsUiState()
  private val settingsState = MutableStateFlow(initialState.settings)

  fun refreshSettings() {
    settingsState.update { settingsRepository.getAll() }
  }

  fun setHideMyShows(hide: Boolean) {
    settingsRepository.isMyShowsHidden = hide
    refreshSettings()
  }

  fun setHideMyRatingsShows(hide: Boolean) {
    settingsRepository.isMyShowsRatingsHidden = hide
    refreshSettings()
  }

  fun setHideWatchlistShows(hide: Boolean) {
    settingsRepository.isWatchlistShowsHidden = hide
    refreshSettings()
  }

  fun setHideWatchlistRatingsShows(hide: Boolean) {
    settingsRepository.isWatchlistShowsRatingsHidden = hide
    refreshSettings()
  }

  fun setHideHiddenShows(hide: Boolean) {
    settingsRepository.isHiddenShowsHidden = hide
    refreshSettings()
  }

  fun setHideHiddenRatingsShows(hide: Boolean) {
    settingsRepository.isHiddenShowsRatingsHidden = hide
    refreshSettings()
  }

  fun setHideNotCollectedShows(hide: Boolean) {
    settingsRepository.isUncollectedShowsHidden = hide
    refreshSettings()
  }

  fun setHideNotCollectedRatingsShows(hide: Boolean) {
    settingsRepository.isUncollectedShowsRatingsHidden = hide
    refreshSettings()
  }

  val uiState = settingsState
    .map { SpoilersShowsUiState(it) }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
      initialValue = SpoilersShowsUiState()
    )
}
