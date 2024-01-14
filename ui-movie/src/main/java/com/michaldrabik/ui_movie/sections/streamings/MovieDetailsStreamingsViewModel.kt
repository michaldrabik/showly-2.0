package com.michaldrabik.ui_movie.sections.streamings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.utilities.extensions.rethrowCancellation
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_movie.sections.streamings.MovieDetailsStreamingsUiState.StreamingsState
import com.michaldrabik.ui_movie.sections.streamings.cases.MovieDetailsStreamingCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieDetailsStreamingsViewModel @Inject constructor(
  private val streamingCase: MovieDetailsStreamingCase,
) : ViewModel() {

  private lateinit var movie: Movie

  private val streamingsState = MutableStateFlow<StreamingsState?>(null)
  private val loadingState = MutableStateFlow(false)

  fun loadStreamings(movie: Movie) {
    if (this::movie.isInitialized) return
    this.movie = movie

    viewModelScope.launch {
      loadingState.value = true
      try {
        val localStreamings = streamingCase.getLocalStreamingServices(movie)
        streamingsState.value = StreamingsState(localStreamings, isLocal = true)

        val remoteStreamings = streamingCase.loadStreamingServices(movie)
        streamingsState.value = StreamingsState(remoteStreamings, isLocal = false)
      } catch (error: Throwable) {
        streamingsState.value = StreamingsState(emptyList(), isLocal = false)
        rethrowCancellation(error)
      } finally {
        loadingState.value = false
      }
    }
  }

  val uiState = combine(
    streamingsState,
    loadingState
  ) { s1, _ ->
    MovieDetailsStreamingsUiState(
      streamings = s1
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = MovieDetailsStreamingsUiState()
  )
}
