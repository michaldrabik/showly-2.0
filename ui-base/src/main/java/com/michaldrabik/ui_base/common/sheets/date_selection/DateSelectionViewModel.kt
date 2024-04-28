package com.michaldrabik.ui_base.common.sheets.date_selection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DateSelectionViewModel @Inject constructor() : ViewModel() {

  private val initialState = DateSelectionUiState()

  private val isLoadingState = MutableStateFlow(initialState.isLoading)

  val uiState = combine(
    isLoadingState
  ) { s1 ->
    DateSelectionUiState(isLoading = s1[0])
  }.stateIn(
    scope = viewModelScope,
    initialValue = initialState,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT)
  )
}
