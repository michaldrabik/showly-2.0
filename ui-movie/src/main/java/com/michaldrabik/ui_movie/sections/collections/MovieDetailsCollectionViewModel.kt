package com.michaldrabik.ui_movie.sections.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.repository.movies.MovieCollectionsRepository.Source
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.utilities.extensions.rethrowCancellation
import com.michaldrabik.ui_base.viewmodel.ChannelsDelegate
import com.michaldrabik.ui_base.viewmodel.DefaultChannelsDelegate
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.MovieCollection
import com.michaldrabik.ui_movie.sections.collections.cases.MovieDetailsCollectionCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MovieDetailsCollectionViewModel @Inject constructor(
  private val collectionsCase: MovieDetailsCollectionCase,
) : ViewModel(), ChannelsDelegate by DefaultChannelsDelegate() {

  private lateinit var movie: Movie

  private val loadingState = MutableStateFlow(true)
  private val movieCollectionState = MutableStateFlow<Pair<List<MovieCollection>, Source>?>(null)

  fun loadMovieCollections(movie: Movie) {
    if (this::movie.isInitialized) {
      return
    }
    this.movie = movie

    viewModelScope.launch {
      try {
        movieCollectionState.value = collectionsCase.loadMovieCollections(movie)
      } catch (error: Throwable) {
        Timber.e(error)
        rethrowCancellation(error)
      } finally {
        loadingState.value = false
      }
    }
    Timber.d("Loading movie collections...")
  }

  val uiState = combine(
    loadingState,
    movieCollectionState
  ) { s1, s2 ->
    MovieDetailsCollectionUiState(
      isLoading = s1,
      collections = s2
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = MovieDetailsCollectionUiState()
  )
}
