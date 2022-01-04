package com.michaldrabik.ui_base.common.sheets.remove_trakt.remove_trakt_watchlist

import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.common.sheets.remove_trakt.RemoveTraktBottomSheet.Mode
import com.michaldrabik.ui_base.common.sheets.remove_trakt.remove_trakt_watchlist.cases.RemoveTraktWatchlistCase
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
class RemoveTraktWatchlistViewModel @Inject constructor(
  private val removeTraktWatchlistCase: RemoveTraktWatchlistCase
) : BaseViewModel() {

  private val loadingState = MutableStateFlow(false)
  private val finishedState = MutableStateFlow(false)

  fun removeFromTrakt(traktIds: List<IdTrakt>, mode: Mode) {
    viewModelScope.launch {
      try {
        loadingState.value = true
        removeTraktWatchlistCase.removeTraktWatchlist(traktIds, mode)
        finishedState.value = true
      } catch (error: Throwable) {
        _messageChannel.send(MessageEvent.error(R.string.errorTraktSyncGeneral))
        loadingState.value = false
      }
    }
  }

  val uiState = combine(
    loadingState,
    finishedState
  ) { s1, s2 ->
    RemoveTraktWatchlistUiState(
      isLoading = s1,
      isFinished = s2
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = RemoveTraktWatchlistUiState()
  )
}
