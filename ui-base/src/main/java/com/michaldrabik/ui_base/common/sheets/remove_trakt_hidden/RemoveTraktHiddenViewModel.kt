package com.michaldrabik.ui_base.common.sheets.remove_trakt_hidden

import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.Mode
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.common.sheets.remove_trakt_hidden.cases.RemoveTraktHiddenCase
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_model.IdTrakt
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RemoveTraktHiddenViewModel @Inject constructor(
  private val removeTraktHiddenCase: RemoveTraktHiddenCase
) : BaseViewModel() {

  private val loadingState = MutableStateFlow(false)
  private val finishedState = MutableStateFlow(false)

  fun removeFromTrakt(idTrakt: IdTrakt, mode: Mode) {
    viewModelScope.launch {
      try {
        loadingState.value = true
        when (mode) {
          Mode.SHOWS -> removeTraktHiddenCase.removeTraktArchive(idTrakt, mode)
          Mode.MOVIES -> removeTraktHiddenCase.removeTraktArchive(idTrakt, mode)
        }
        finishedState.value = true
      } catch (error: Throwable) {
        _messageState.emit(MessageEvent.error(R.string.errorTraktSyncGeneral))
        loadingState.value = false
      }
    }
  }

  val uiState = combine(
    loadingState,
    finishedState
  ) { s1, s2 ->
    RemoveTraktHiddenUiState(
      isLoading = s1,
      isFinished = s2
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = RemoveTraktHiddenUiState()
  )
}
