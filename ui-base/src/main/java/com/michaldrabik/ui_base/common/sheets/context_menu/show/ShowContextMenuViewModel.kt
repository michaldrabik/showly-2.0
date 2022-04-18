package com.michaldrabik.ui_base.common.sheets.context_menu.show

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.repository.images.ShowImagesProvider
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.common.sheets.context_menu.events.FinishUiEvent
import com.michaldrabik.ui_base.common.sheets.context_menu.events.RemoveTraktUiEvent
import com.michaldrabik.ui_base.common.sheets.context_menu.show.cases.ShowContextMenuHiddenCase
import com.michaldrabik.ui_base.common.sheets.context_menu.show.cases.ShowContextMenuLoadItemCase
import com.michaldrabik.ui_base.common.sheets.context_menu.show.cases.ShowContextMenuMyShowsCase
import com.michaldrabik.ui_base.common.sheets.context_menu.show.cases.ShowContextMenuOnHoldCase
import com.michaldrabik.ui_base.common.sheets.context_menu.show.cases.ShowContextMenuPinnedCase
import com.michaldrabik.ui_base.common.sheets.context_menu.show.cases.ShowContextMenuWatchlistCase
import com.michaldrabik.ui_base.common.sheets.context_menu.show.helpers.ShowContextItem
import com.michaldrabik.ui_base.network.NetworkStatusProvider
import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_base.utilities.events.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.utilities.extensions.rethrowCancellation
import com.michaldrabik.ui_base.viewmodel.ChannelsDelegate
import com.michaldrabik.ui_base.viewmodel.DefaultChannelsDelegate
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.ImageType
import dagger.hilt.android.lifecycle.HiltViewModel
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
  private val loadItemCase: ShowContextMenuLoadItemCase,
  private val myShowsCase: ShowContextMenuMyShowsCase,
  private val watchlistCase: ShowContextMenuWatchlistCase,
  private val hiddenCase: ShowContextMenuHiddenCase,
  private val pinnedCase: ShowContextMenuPinnedCase,
  private val onHoldCase: ShowContextMenuOnHoldCase,
  private val imagesProvider: ShowImagesProvider,
  private val networkProvider: NetworkStatusProvider,
  private val settingsRepository: SettingsRepository
) : ViewModel(), ChannelsDelegate by DefaultChannelsDelegate() {

  private var showId by notNull<IdTrakt>()
  private var isQuickRemoveEnabled by notNull<Boolean>()

  private val loadingState = MutableStateFlow(false)
  private val itemState = MutableStateFlow<ShowContextItem?>(null)

  fun loadShow(idTrakt: IdTrakt) {
    viewModelScope.launch {
      showId = idTrakt
      isQuickRemoveEnabled = settingsRepository.load().traktQuickRemoveEnabled

      try {
        loadingState.value = true
        val item = loadItemCase.loadItem(idTrakt)
        itemState.value = item
      } catch (error: Throwable) {
        messageChannel.send(MessageEvent.Error(R.string.errorGeneral))
      } finally {
        loadingState.value = false
      }
    }
  }

  fun moveToMyShows() {
    viewModelScope.launch {
      if (!networkProvider.isOnline()) {
        messageChannel.send(MessageEvent.Error(R.string.errorNoInternetConnection))
        return@launch
      }
      try {
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
        myShowsCase.removeFromMyShows(
          traktId = showId,
          removeLocalData = networkProvider.isOnline()
        )
        checkQuickRemove(RemoveTraktUiEvent(removeProgress = true))
      } catch (error: Throwable) {
        onError(error)
      }
    }
  }

  fun moveToWatchlist() {
    viewModelScope.launch {
      try {
        val result = watchlistCase.moveToWatchlist(
          traktId = showId,
          removeLocalData = networkProvider.isOnline()
        )
        checkQuickRemove(result)
      } catch (error: Throwable) {
        onError(error)
      }
    }
  }

  fun removeFromWatchlist() {
    viewModelScope.launch {
      try {
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
        val result = hiddenCase.moveToHidden(
          traktId = showId,
          removeLocalData = networkProvider.isOnline()
        )
        checkQuickRemove(result)
      } catch (error: Throwable) {
        onError(error)
      }
    }
  }

  fun removeFromHidden() {
    viewModelScope.launch {
      try {
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
      eventChannel.send(Event(FinishUiEvent(true)))
    }
  }

  fun removeFromTopPinned() {
    viewModelScope.launch {
      pinnedCase.removeFromTopPinned(showId)
      eventChannel.send(Event(FinishUiEvent(true)))
    }
  }

  fun addToOnHoldPinned() {
    viewModelScope.launch {
      onHoldCase.addToOnHold(showId)
      eventChannel.send(Event(FinishUiEvent(true)))
    }
  }

  fun removeFromOnHoldPinned() {
    viewModelScope.launch {
      onHoldCase.removeFromOnHold(showId)
      eventChannel.send(Event(FinishUiEvent(true)))
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
      loadingState.value = false
      eventChannel.send(Event(event))
    } else {
      eventChannel.send(Event(FinishUiEvent(true)))
    }
  }

  private suspend fun onError(error: Throwable) {
    loadingState.value = false
    messageChannel.send(MessageEvent.Error(R.string.errorGeneral))
    rethrowCancellation(error)
  }

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
