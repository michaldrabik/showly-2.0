package com.michaldrabik.ui_settings.sections.spoilers.movies

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
class SpoilersMoviesViewModel @Inject constructor(
  private val settingsRepository: SettingsSpoilersRepository
) : ViewModel() {

  private val initialState = SpoilersMoviesUiState()

  private val isMyMoviesHiddenState = MutableStateFlow(initialState.isMyMoviesHidden)
  private val isWatchlistMoviesHiddenState = MutableStateFlow(initialState.isWatchlistMoviesHidden)
  private val isHiddenMoviesHiddenState = MutableStateFlow(initialState.isHiddenMoviesHidden)
  private val isNotCollectedMoviesHiddenState = MutableStateFlow(initialState.isNotCollectedMoviesHidden)

  fun refreshSettings() {
    isMyMoviesHiddenState.value = settingsRepository.isMyMoviesHidden
    isWatchlistMoviesHiddenState.value = settingsRepository.isWatchlistMoviesHidden
    isHiddenMoviesHiddenState.value = settingsRepository.isHiddenMoviesHidden
    isNotCollectedMoviesHiddenState.value = settingsRepository.isUncollectedMoviesHidden
  }

  fun setHideMyMovies(hide: Boolean) {
    settingsRepository.isMyMoviesHidden = hide
    refreshSettings()
  }

  fun setHideWatchlistMovies(hide: Boolean) {
    settingsRepository.isWatchlistMoviesHidden = hide
    refreshSettings()
  }

  fun setHideHiddenMovies(hide: Boolean) {
    settingsRepository.isHiddenMoviesHidden = hide
    refreshSettings()
  }

  fun setHideNotCollectedMovies(hide: Boolean) {
    settingsRepository.isUncollectedMoviesHidden = hide
    refreshSettings()
  }

  val uiState = combine(
    isMyMoviesHiddenState,
    isWatchlistMoviesHiddenState,
    isHiddenMoviesHiddenState,
    isNotCollectedMoviesHiddenState
  ) { s1, s2, s3, s4 ->
    SpoilersMoviesUiState(
      isMyMoviesHidden = s1,
      isWatchlistMoviesHidden = s2,
      isHiddenMoviesHidden = s3,
      isNotCollectedMoviesHidden = s4,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = SpoilersMoviesUiState()
  )
}
