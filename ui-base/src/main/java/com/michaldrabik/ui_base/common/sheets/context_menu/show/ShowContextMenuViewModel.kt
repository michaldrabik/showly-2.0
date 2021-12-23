package com.michaldrabik.ui_base.common.sheets.context_menu.show

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.common.OnlineStatusProvider
import com.michaldrabik.ui_base.common.sheets.context_menu.show.cases.ShowContextMenuLoadItemCase
import com.michaldrabik.ui_base.common.sheets.context_menu.show.cases.ShowContextMenuWatchlistCase
import com.michaldrabik.ui_base.common.sheets.context_menu.show.helpers.ShowContextItem
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_model.IdTrakt
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
@HiltViewModel
class ShowContextMenuViewModel @Inject constructor(
  @ApplicationContext private val context: Context,
  private val loadItemCase: ShowContextMenuLoadItemCase,
  private val watchlistCase: ShowContextMenuWatchlistCase
) : BaseViewModel() {

  private val loadingState = MutableStateFlow(false)
  private val finishedState = MutableStateFlow(false)
  private val itemState = MutableStateFlow<ShowContextItem?>(null)

  fun loadShow(showId: IdTrakt) {
    viewModelScope.launch {
      try {
        val item = loadItemCase.loadItem(showId)
        itemState.value = item
      } catch (error: Throwable) {
        _messageChannel.send(MessageEvent.error(R.string.errorGeneral))
      }
    }
  }

  fun moveToWatchlist(showId: IdTrakt) {
    viewModelScope.launch {
      try {
        loadingState.value = true
        watchlistCase.moveToWatchlist(showId, removeLocalData = isOnline())
        finishedState.value = true
      } catch (error: Throwable) {
        loadingState.value = false
        _messageChannel.send(MessageEvent.error(R.string.errorGeneral))
        rethrowCancellation(error)
      }
    }
  }

  fun removeToWatchlist(showId: IdTrakt) {
    viewModelScope.launch {
      try {
        loadingState.value = true
        watchlistCase.removeFromWatchlist(showId)
        finishedState.value = true
      } catch (error: Throwable) {
        loadingState.value = false
        _messageChannel.send(MessageEvent.error(R.string.errorGeneral))
        rethrowCancellation(error)
      }
    }
  }

  private fun isOnline() = (context as OnlineStatusProvider).isOnline()

  val uiState = combine(
    loadingState,
    finishedState,
    itemState
  ) { s1, s2, s3 ->
    ShowContextMenuUiState(
      isLoading = s1,
      isFinished = s2,
      item = s3
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = ShowContextMenuUiState()
  )
}
