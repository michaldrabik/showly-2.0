package com.michaldrabik.ui_movie.sections.related

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.repository.images.MovieImagesProvider
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_base.utilities.extensions.rethrowCancellation
import com.michaldrabik.ui_base.viewmodel.ChannelsDelegate
import com.michaldrabik.ui_base.viewmodel.DefaultChannelsDelegate
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_movie.cases.MovieDetailsMyMoviesCase
import com.michaldrabik.ui_movie.sections.related.cases.MovieDetailsRelatedCase
import com.michaldrabik.ui_movie.sections.related.recycler.RelatedListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
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

  private lateinit var movie: Movie

  private val loadingState = MutableStateFlow(true)
  private val relatedItemsState = MutableStateFlow<List<RelatedListItem>?>(null)

  fun loadRelatedMovies(movie: Movie) {
    if (this::movie.isInitialized) return
    this.movie = movie

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
        relatedItemsState.value = related
      } catch (error: Throwable) {
        relatedItemsState.value = emptyList()
        Timber.e(error)
        rethrowCancellation(error)
      } finally {
        loadingState.value = false
      }
    }
    Timber.d("Loading related movies...")
  }

  fun loadMissingImage(item: RelatedListItem, force: Boolean) {

    fun updateItem(new: RelatedListItem) {
      val currentItems = uiState.value.relatedMovies?.toMutableList()
      currentItems?.findReplace(new) { it isSameAs new }
      relatedItemsState.value = currentItems
    }

    viewModelScope.launch {
      updateItem(item.copy(isLoading = true))
      try {
        val image = imagesProvider.loadRemoteImage(item.movie, item.image.type, force)
        updateItem(item.copy(isLoading = false, image = image))
      } catch (t: Throwable) {
        updateItem(item.copy(isLoading = false, image = Image.createUnavailable(item.image.type)))
      }
    }
  }

  val uiState = combine(
    loadingState,
    relatedItemsState
  ) { s1, s2 ->
    MovieDetailsRelatedUiState(
      isLoading = s1,
      relatedMovies = s2
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = MovieDetailsRelatedUiState()
  )
}
