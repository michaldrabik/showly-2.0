package com.michaldrabik.ui_base.common.sheets.remove_trakt.remove_trakt_progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.common.sheets.remove_trakt.RemoveTraktBottomSheet.Mode
import com.michaldrabik.ui_base.common.sheets.remove_trakt.remove_trakt_progress.cases.RemoveTraktProgressCase
import com.michaldrabik.ui_base.utilities.events.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.viewmodel.ChannelsDelegate
import com.michaldrabik.ui_base.viewmodel.DefaultChannelsDelegate
import com.michaldrabik.ui_model.IdTrakt
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RemoveTraktProgressViewModel @Inject constructor(
  private val removeTraktProgressCase: RemoveTraktProgressCase
) : ViewModel(), ChannelsDelegate by DefaultChannelsDelegate() {

  private val loadingState = MutableStateFlow(false)
  private val finishedState = MutableStateFlow(false)

  fun removeFromTrakt(traktIds: List<IdTrakt>, mode: Mode) {
    viewModelScope.launch {
      try {
        loadingState.value = true
        removeTraktProgressCase.removeTraktProgress(traktIds, mode)
        finishedState.value = true
      } catch (error: Throwable) {
        messageChannel.send(MessageEvent.Error(R.string.errorTraktSyncGeneral))
        loadingState.value = false
      }
    }
  }

  val uiState = combine(
    loadingState,
    finishedState
  ) { s1, s2 ->
    RemoveTraktProgressUiState(
      isLoading = s1,
      isFinished = s2
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = RemoveTraktProgressUiState()
  )
}
