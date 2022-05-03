package com.michaldrabik.ui_movie.related

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.repository.images.MovieImagesProvider
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.utilities.extensions.combine
import com.michaldrabik.ui_base.utilities.extensions.rethrowCancellation
import com.michaldrabik.ui_base.viewmodel.ChannelsDelegate
import com.michaldrabik.ui_base.viewmodel.DefaultChannelsDelegate
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_movie.MovieDetailsUiState
import com.michaldrabik.ui_movie.cases.MovieDetailsMyMoviesCase
import com.michaldrabik.ui_movie.cases.MovieDetailsRelatedCase
import com.michaldrabik.ui_movie.related.recycler.RelatedListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MovieDetailsRelatedViewModel @Inject constructor(
  private val relatedCase: MovieDetailsRelatedCase,
  private val myMoviesCase: MovieDetailsMyMoviesCase,
  private val imagesProvider: MovieImagesProvider,
) : ViewModel(), ChannelsDelegate by DefaultChannelsDelegate() {

  private val relatedState = MutableStateFlow<List<RelatedListItem>?>(null)

  private var isLoading = false

  fun onParentState(state: MovieDetailsUiState) {
    state.movie?.let { loadRelatedMovies(it) }
  }

  private fun loadRelatedMovies(movie: Movie) {
    if (isLoading) return
    viewModelScope.launch {
      try {
        val (myMovies, watchlistMovies) = myMoviesCase.getAllIds()
        val related = relatedCase.loadRelatedMovies(movie).map {
          val image = imagesProvider.findCachedImage(it, ImageType.POSTER)
          RelatedListItem(
            movie = it,
            image = image,
            isFollowed = it.traktId in myMovies,
            isWatchlist = it.traktId in watchlistMovies
          )
        }
        relatedState.value = related
      } catch (error: Throwable) {
        relatedState.value = emptyList()
        Timber.e(error)
        rethrowCancellation(error)
      } finally {
        isLoading = false
      }
    }
    isLoading = true
    Timber.d("Loading related movies...")
  }

  val uiState = combine(
    relatedState,
  ) { s1 ->
    MovieDetailsRelatedUiState(
      relatedMovies = s1
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = MovieDetailsRelatedUiState()
  )
}
