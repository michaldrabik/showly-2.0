package com.michaldrabik.ui_movie.sections.collections.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.repository.movies.MovieCollectionsRepository.Source
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.utilities.extensions.rethrowCancellation
import com.michaldrabik.ui_base.viewmodel.ChannelsDelegate
import com.michaldrabik.ui_base.viewmodel.DefaultChannelsDelegate
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.MovieCollection
import com.michaldrabik.ui_movie.MovieDetailsEvent.OpenCollectionSheet
import com.michaldrabik.ui_movie.sections.collections.list.cases.MovieDetailsCollectionsCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MovieDetailsCollectionsViewModel @Inject constructor(
  private val collectionsCase: MovieDetailsCollectionsCase,
) : ViewModel(), ChannelsDelegate by DefaultChannelsDelegate() {

  private lateinit var movie: Movie
  private var lastOpenedCollection: IdTrakt? = null

  private val loadingState = MutableStateFlow(true)
  private val movieCollectionState = MutableStateFlow<Pair<List<MovieCollection>, Source>?>(null)

  fun loadCollections(movie: Movie) {
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

  fun loadCollection(collection: MovieCollection) {
    viewModelScope.launch {
      eventChannel.send(OpenCollectionSheet(movie, collection))
    }
  }

  fun loadLastOpenedCollection() {
    lastOpenedCollection?.let { id ->
      val collection = movieCollectionState.value?.first?.find { it.id == id }
      collection?.let {
        loadCollection(collection)
        lastOpenedCollection = null
      }
    }
  }

  fun saveLastOpenedCollection(collectionId: IdTrakt) {
    lastOpenedCollection = collectionId
  }

  val uiState = combine(
    loadingState,
    movieCollectionState
  ) { s1, s2 ->
    MovieDetailsCollectionsUiState(
      isLoading = s1,
      collections = s2
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = MovieDetailsCollectionsUiState()
  )
}
