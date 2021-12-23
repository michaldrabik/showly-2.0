package com.michaldrabik.ui_base.common.sheets.context_menu.show

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.common.OnlineStatusProvider
import com.michaldrabik.ui_base.common.sheets.context_menu.show.cases.ShowContextMenuHiddenCase
import com.michaldrabik.ui_base.common.sheets.context_menu.show.cases.ShowContextMenuLoadItemCase
import com.michaldrabik.ui_base.common.sheets.context_menu.show.cases.ShowContextMenuMyShowsCase
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
import kotlin.properties.Delegates.notNull

@SuppressLint("StaticFieldLeak")
@HiltViewModel
class ShowContextMenuViewModel @Inject constructor(
  @ApplicationContext private val context: Context,
  private val loadItemCase: ShowContextMenuLoadItemCase,
  private val myShowsCase: ShowContextMenuMyShowsCase,
  private val watchlistCase: ShowContextMenuWatchlistCase,
  private val hiddenCase: ShowContextMenuHiddenCase
) : BaseViewModel() {

  private var showId by notNull<IdTrakt>()

  private val loadingState = MutableStateFlow(false)
  private val finishedState = MutableStateFlow(false)
  private val itemState = MutableStateFlow<ShowContextItem?>(null)

  fun loadShow(showId: IdTrakt) {
    this.showId = showId
    viewModelScope.launch {
      try {
        val item = loadItemCase.loadItem(showId)
        itemState.value = item
      } catch (error: Throwable) {
        _messageChannel.send(MessageEvent.error(R.string.errorGeneral))
      }
    }
  }

  fun moveToMyShows() {
    viewModelScope.launch {
      if (!isOnline()) {
        _messageChannel.send(MessageEvent.error(R.string.errorNoInternetConnection))
        return@launch
      }
      try {
        loadingState.value = true
        myShowsCase.moveToMyShows(showId)
        finishedState.value = true
      } catch (error: Throwable) {
        onError(error)
      }
    }
  }

  fun removeFromMyShows() {
    viewModelScope.launch {
      try {
        loadingState.value = true
        myShowsCase.removeFromMyShows(showId, removeLocalData = isOnline())
        finishedState.value = true
      } catch (error: Throwable) {
        onError(error)
      }
    }
  }

  fun moveToWatchlist() {
    viewModelScope.launch {
      try {
        loadingState.value = true
        watchlistCase.moveToWatchlist(showId, removeLocalData = isOnline())
        finishedState.value = true
      } catch (error: Throwable) {
        onError(error)
      }
    }
  }

  fun removeFromWatchlist() {
    viewModelScope.launch {
      try {
        loadingState.value = true
        watchlistCase.removeFromWatchlist(showId)
        finishedState.value = true
      } catch (error: Throwable) {
        onError(error)
      }
    }
  }

  fun moveToHidden() {
    viewModelScope.launch {
      try {
        loadingState.value = true
        hiddenCase.moveToHidden(showId, removeLocalData = isOnline())
        finishedState.value = true
      } catch (error: Throwable) {
        onError(error)
      }
    }
  }

  fun removeFromHidden() {
    viewModelScope.launch {
      try {
        loadingState.value = true
        hiddenCase.removeFromHidden(showId)
        finishedState.value = true
      } catch (error: Throwable) {
        onError(error)
      }
    }
  }

  private suspend fun onError(error: Throwable) {
    loadingState.value = false
    _messageChannel.send(MessageEvent.error(R.string.errorGeneral))
    rethrowCancellation(error)
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
