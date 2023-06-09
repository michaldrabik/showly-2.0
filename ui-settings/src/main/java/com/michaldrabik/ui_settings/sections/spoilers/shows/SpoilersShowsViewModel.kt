package com.michaldrabik.ui_settings.sections.spoilers.shows

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.repository.settings.SettingsSpoilersRepository
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SpoilersShowsViewModel @Inject constructor(
  private val settingsRepository: SettingsSpoilersRepository
) : ViewModel() {

  private val initialState = SpoilersShowsUiState()

  private val isMyShowsHiddenState = MutableStateFlow(initialState.isMyShowsHidden)
  private val isWatchlistShowsHiddenState = MutableStateFlow(initialState.isWatchlistShowsHidden)
  private val isHiddenShowsHiddenState = MutableStateFlow(initialState.isHiddenShowsHidden)
  private val isNotCollectedShowsHiddenState = MutableStateFlow(initialState.isNotCollectedShowsHidden)

  fun refreshSettings() {
    isMyShowsHiddenState.value = settingsRepository.isMyShowsHidden
    isWatchlistShowsHiddenState.value = settingsRepository.isWatchlistShowsHidden
    isHiddenShowsHiddenState.value = settingsRepository.isHiddenShowsHidden
    isNotCollectedShowsHiddenState.value = settingsRepository.isUncollectedShowsHidden
  }

  fun setHideMyShows(hide: Boolean) {
    settingsRepository.isMyShowsHidden = hide
    refreshSettings()
  }

  fun setHideWatchlistShows(hide: Boolean) {
    settingsRepository.isWatchlistShowsHidden = hide
    refreshSettings()
  }

  fun setHideHiddenShows(hide: Boolean) {
    settingsRepository.isHiddenShowsHidden = hide
    refreshSettings()
  }

  fun setHideNotCollectedShows(hide: Boolean) {
    settingsRepository.isUncollectedShowsHidden = hide
    refreshSettings()
  }

  val uiState = combine(
    isMyShowsHiddenState,
    isWatchlistShowsHiddenState,
    isHiddenShowsHiddenState,
    isNotCollectedShowsHiddenState
  ) { s1, s2, s3, s4 ->
    SpoilersShowsUiState(
      isMyShowsHidden = s1,
      isWatchlistShowsHidden = s2,
      isHiddenShowsHidden = s3,
      isNotCollectedShowsHidden = s4,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = SpoilersShowsUiState()
  )
}
