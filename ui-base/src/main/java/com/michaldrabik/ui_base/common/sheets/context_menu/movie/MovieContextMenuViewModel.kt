package com.michaldrabik.ui_base.common.sheets.context_menu.movie

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.viewModelScope
import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.common.sheets.context_menu.events.FinishUiEvent
import com.michaldrabik.ui_base.common.sheets.context_menu.events.RemoveTraktUiEvent
import com.michaldrabik.ui_base.common.sheets.context_menu.movie.cases.MovieContextMenuHiddenCase
import com.michaldrabik.ui_base.common.sheets.context_menu.movie.cases.MovieContextMenuLoadItemCase
import com.michaldrabik.ui_base.common.sheets.context_menu.movie.cases.MovieContextMenuMyMoviesCase
import com.michaldrabik.ui_base.common.sheets.context_menu.movie.cases.MovieContextMenuPinnedCase
import com.michaldrabik.ui_base.common.sheets.context_menu.movie.cases.MovieContextMenuWatchlistCase
import com.michaldrabik.ui_base.common.sheets.context_menu.movie.helpers.MovieContextItem
import com.michaldrabik.ui_base.utilities.Event
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
class MovieContextMenuViewModel @Inject constructor(
  @ApplicationContext private val context: Context,
  private val loadItemCase: MovieContextMenuLoadItemCase,
  private val myMoviesCase: MovieContextMenuMyMoviesCase,
  private val watchlistCase: MovieContextMenuWatchlistCase,
  private val hiddenCase: MovieContextMenuHiddenCase,
  private val pinnedCase: MovieContextMenuPinnedCase,
  private val settingsRepository: SettingsRepository
) : BaseViewModel() {

  private var movieId by notNull<IdTrakt>()
  private var isQuickRemoveEnabled by notNull<Boolean>()

  private val loadingState = MutableStateFlow(false)
  private val itemState = MutableStateFlow<MovieContextItem?>(null)

  fun loadMovie(idTrakt: IdTrakt) {
    viewModelScope.launch {
      movieId = idTrakt
      isQuickRemoveEnabled = settingsRepository.load().traktQuickRemoveEnabled

      try {
        val item = loadItemCase.loadItem(idTrakt)
        itemState.value = item
      } catch (error: Throwable) {
        _messageChannel.send(MessageEvent.error(R.string.errorGeneral))
      }
    }
  }

  fun moveToMyMovies() {
    viewModelScope.launch {
      try {
        loadingState.value = true
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
        loadingState.value = true
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
        loadingState.value = true
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
        loadingState.value = true
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
        loadingState.value = true
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
        loadingState.value = true
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
      _eventChannel.send(Event(FinishUiEvent(true)))
    }
  }

  fun removeFromTopPinned() {
    viewModelScope.launch {
      pinnedCase.removeFromTopPinned(movieId)
      _eventChannel.send(Event(FinishUiEvent(true)))
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
