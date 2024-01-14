package com.michaldrabik.ui_settings.sections.spoilers.movies

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
class SpoilersMoviesViewModel @Inject constructor(
  private val settingsRepository: SettingsSpoilersRepository
) : ViewModel() {

  private val initialState = SpoilersMoviesUiState()
  private val settingsState = MutableStateFlow(initialState.settings)

  fun refreshSettings() {
    settingsState.update { settingsRepository.getAll() }
  }

  fun setHideMyMovies(hide: Boolean) {
    settingsRepository.isMyMoviesHidden = hide
    refreshSettings()
  }

  fun setHideMyRatingsMovies(hide: Boolean) {
    settingsRepository.isMyMoviesRatingsHidden = hide
    refreshSettings()
  }

  fun setHideWatchlistMovies(hide: Boolean) {
    settingsRepository.isWatchlistMoviesHidden = hide
    refreshSettings()
  }

  fun setHideWatchlistRatingsMovies(hide: Boolean) {
    settingsRepository.isWatchlistMoviesRatingsHidden = hide
    refreshSettings()
  }

  fun setHideHiddenMovies(hide: Boolean) {
    settingsRepository.isHiddenMoviesHidden = hide
    refreshSettings()
  }

  fun setHideHiddenRatingsMovies(hide: Boolean) {
    settingsRepository.isHiddenMoviesRatingsHidden = hide
    refreshSettings()
  }

  fun setHideNotCollectedMovies(hide: Boolean) {
    settingsRepository.isUncollectedMoviesHidden = hide
    refreshSettings()
  }

  fun setHideNotCollectedRatingsMovies(hide: Boolean) {
    settingsRepository.isUncollectedMoviesRatingsHidden = hide
    refreshSettings()
  }

  val uiState = settingsState
    .map { SpoilersMoviesUiState(it) }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
      initialValue = SpoilersMoviesUiState()
    )
}
