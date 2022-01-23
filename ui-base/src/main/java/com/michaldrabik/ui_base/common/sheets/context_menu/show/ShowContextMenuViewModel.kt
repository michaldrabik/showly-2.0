package com.michaldrabik.ui_base.common.sheets.context_menu.show

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.viewModelScope
import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.common.OnlineStatusProvider
import com.michaldrabik.ui_base.common.sheets.context_menu.events.FinishUiEvent
import com.michaldrabik.ui_base.common.sheets.context_menu.events.RemoveTraktUiEvent
import com.michaldrabik.ui_base.common.sheets.context_menu.show.cases.ShowContextMenuHiddenCase
import com.michaldrabik.ui_base.common.sheets.context_menu.show.cases.ShowContextMenuLoadItemCase
import com.michaldrabik.ui_base.common.sheets.context_menu.show.cases.ShowContextMenuMyShowsCase
import com.michaldrabik.ui_base.common.sheets.context_menu.show.cases.ShowContextMenuOnHoldCase
import com.michaldrabik.ui_base.common.sheets.context_menu.show.cases.ShowContextMenuPinnedCase
import com.michaldrabik.ui_base.common.sheets.context_menu.show.cases.ShowContextMenuWatchlistCase
import com.michaldrabik.ui_base.common.sheets.context_menu.show.helpers.ShowContextItem
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_base.utilities.Event
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.ImageType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.properties.Delegates.notNull

@SuppressLint("StaticFieldLeak")
@HiltViewModel
class ShowContextMenuViewModel @Inject constructor(
  @ApplicationContext private val context: Context,
  private val loadItemCase: ShowContextMenuLoadItemCase,
  private val myShowsCase: ShowContextMenuMyShowsCase,
  private val watchlistCase: ShowContextMenuWatchlistCase,
  private val hiddenCase: ShowContextMenuHiddenCase,
  private val pinnedCase: ShowContextMenuPinnedCase,
  private val onHoldCase: ShowContextMenuOnHoldCase,
  private val imagesProvider: ShowImagesProvider,
  private val settingsRepository: SettingsRepository
) : BaseViewModel() {

  private var showId by notNull<IdTrakt>()
  private var isQuickRemoveEnabled by notNull<Boolean>()

  private val loadingState = MutableStateFlow(false)
  private val itemState = MutableStateFlow<ShowContextItem?>(null)

  fun loadShow(idTrakt: IdTrakt) {
    viewModelScope.launch {
      showId = idTrakt
      isQuickRemoveEnabled = settingsRepository.load().traktQuickRemoveEnabled

      try {
        val item = loadItemCase.loadItem(idTrakt)
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
        val result = myShowsCase.moveToMyShows(showId)
        preloadImage()
        checkQuickRemove(result)
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
        checkQuickRemove(RemoveTraktUiEvent(removeProgress = true))
      } catch (error: Throwable) {
        onError(error)
      }
    }
  }

  fun moveToWatchlist() {
    viewModelScope.launch {
      try {
        loadingState.value = true
        val result = watchlistCase.moveToWatchlist(showId, removeLocalData = isOnline())
        checkQuickRemove(result)
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
        checkQuickRemove(RemoveTraktUiEvent(removeWatchlist = true))
      } catch (error: Throwable) {
        onError(error)
      }
    }
  }

  fun moveToHidden() {
    viewModelScope.launch {
      try {
        loadingState.value = true
        val result = hiddenCase.moveToHidden(showId, removeLocalData = isOnline())
        checkQuickRemove(result)
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
        checkQuickRemove(RemoveTraktUiEvent(removeHidden = true))
      } catch (error: Throwable) {
        onError(error)
      }
    }
  }

  fun addToTopPinned() {
    viewModelScope.launch {
      pinnedCase.addToTopPinned(showId)
      _eventChannel.send(Event(FinishUiEvent(true)))
    }
  }

  fun removeFromTopPinned() {
    viewModelScope.launch {
      pinnedCase.removeFromTopPinned(showId)
      _eventChannel.send(Event(FinishUiEvent(true)))
    }
  }

  fun addToOnHoldPinned() {
    viewModelScope.launch {
      onHoldCase.addToOnHold(showId)
      _eventChannel.send(Event(FinishUiEvent(true)))
    }
  }

  fun removeFromOnHoldPinned() {
    viewModelScope.launch {
      onHoldCase.removeFromOnHold(showId)
      _eventChannel.send(Event(FinishUiEvent(true)))
    }
  }

  private suspend fun preloadImage() {
    try {
      val show = itemState.value?.show
      show?.let {
        imagesProvider.loadRemoteImage(it, ImageType.FANART)
      }
    } catch (error: Throwable) {
      Timber.e(error)
      rethrowCancellation(error)
    }
  }

  private suspend fun checkQuickRemove(event: RemoveTraktUiEvent) {
    if (isQuickRemoveEnabled) {
      _eventChannel.send(Event(event))
    } else {
      _eventChannel.send(Event(FinishUiEvent(true)))
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
    itemState
  ) { s1, s2 ->
    ShowContextMenuUiState(
      isLoading = s1,
      item = s2
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = ShowContextMenuUiState()
  )
}
