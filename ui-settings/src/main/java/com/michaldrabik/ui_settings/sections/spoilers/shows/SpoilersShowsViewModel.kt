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

  private val isDetailsHiddenState = MutableStateFlow(initialState.isDetailsHidden)
  private val isListsHiddenState = MutableStateFlow(initialState.isListsHidden)

  fun refreshSettings() {
    isDetailsHiddenState.value = settingsRepository.isShowsDetailsHidden
    isListsHiddenState.value = settingsRepository.isShowsListsHidden
  }

  fun setHideDetails(hide: Boolean) {
    settingsRepository.isShowsDetailsHidden = hide
    refreshSettings()
  }

  fun setHideLists(hide: Boolean) {
    settingsRepository.isShowsListsHidden = hide
    refreshSettings()
  }

  val uiState = combine(
    isDetailsHiddenState,
    isListsHiddenState
  ) { s1, s2 ->
    SpoilersShowsUiState(
      isDetailsHidden = s1,
      isListsHidden = s2
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = SpoilersShowsUiState()
  )
}
