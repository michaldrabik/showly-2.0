package com.michaldrabik.ui_base.common.sheets.context_menu.movie

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.common.sheets.context_menu.events.FinishUiEvent
import com.michaldrabik.ui_base.common.sheets.context_menu.events.RemoveTraktUiEvent
import com.michaldrabik.ui_base.common.sheets.context_menu.movie.cases.MovieContextMenuHiddenCase
import com.michaldrabik.ui_base.common.sheets.context_menu.movie.cases.MovieContextMenuLoadItemCase
import com.michaldrabik.ui_base.common.sheets.context_menu.movie.cases.MovieContextMenuMyMoviesCase
import com.michaldrabik.ui_base.common.sheets.context_menu.movie.cases.MovieContextMenuPinnedCase
import com.michaldrabik.ui_base.common.sheets.context_menu.movie.cases.MovieContextMenuWatchlistCase
import com.michaldrabik.ui_base.common.sheets.context_menu.movie.helpers.MovieContextItem
import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_base.utilities.events.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.utilities.extensions.rethrowCancellation
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
import kotlin.properties.Delegates.notNull

@SuppressLint("StaticFieldLeak")
@HiltViewModel
class MovieContextMenuViewModel @Inject constructor(
  private val loadItemCase: MovieContextMenuLoadItemCase,
  private val myMoviesCase: MovieContextMenuMyMoviesCase,
  private val watchlistCase: MovieContextMenuWatchlistCase,
  private val hiddenCase: MovieContextMenuHiddenCase,
  private val pinnedCase: MovieContextMenuPinnedCase,
  private val settingsRepository: SettingsRepository
) : ViewModel(), ChannelsDelegate by DefaultChannelsDelegate() {

  private var movieId by notNull<IdTrakt>()
  private var isQuickRemoveEnabled by notNull<Boolean>()

  private val loadingState = MutableStateFlow(false)
  private val itemState = MutableStateFlow<MovieContextItem?>(null)

  fun loadMovie(idTrakt: IdTrakt) {
    viewModelScope.launch {
      movieId = idTrakt
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

  fun moveToMyMovies() {
    viewModelScope.launch {
      try {
        val result = myMoviesCase.moveToMyMovies(movieId)
        checkQuickRemove(result)
      } catch (error: Throwable) {
        onError(error)
      }
    }
  }

  fun removeFromMyMovies() {
    viewModelScope.launch {
      try {
        myMoviesCase.removeFromMyMovies(movieId)
        checkQuickRemove(RemoveTraktUiEvent(removeProgress = true))
      } catch (error: Throwable) {
        onError(error)
      }
    }
  }

  fun moveToWatchlist() {
    viewModelScope.launch {
      try {
        val result = watchlistCase.moveToWatchlist(movieId)
        checkQuickRemove(result)
      } catch (error: Throwable) {
        onError(error)
      }
    }
  }

  fun removeFromWatchlist() {
    viewModelScope.launch {
      try {
        watchlistCase.removeFromWatchlist(movieId)
        checkQuickRemove(RemoveTraktUiEvent(removeWatchlist = true))
      } catch (error: Throwable) {
        onError(error)
      }
    }
  }

  fun moveToHidden() {
    viewModelScope.launch {
      try {
        val result = hiddenCase.moveToHidden(movieId)
        checkQuickRemove(result)
      } catch (error: Throwable) {
        onError(error)
      }
    }
  }

  fun removeFromHidden() {
    viewModelScope.launch {
      try {
        hiddenCase.removeFromHidden(movieId)
        checkQuickRemove(RemoveTraktUiEvent(removeHidden = true))
      } catch (error: Throwable) {
        onError(error)
      }
    }
  }

  fun addToTopPinned() {
    viewModelScope.launch {
      pinnedCase.addToTopPinned(movieId)
      eventChannel.send(Event(FinishUiEvent(true)))
    }
  }

  fun removeFromTopPinned() {
    viewModelScope.launch {
      pinnedCase.removeFromTopPinned(movieId)
      eventChannel.send(Event(FinishUiEvent(true)))
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
    MovieContextMenuUiState(
      isLoading = s1,
      item = s2
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = MovieContextMenuUiState()
  )
}
