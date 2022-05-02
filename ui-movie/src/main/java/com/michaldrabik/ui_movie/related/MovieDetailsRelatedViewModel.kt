package com.michaldrabik.ui_movie.related

import androidx.lifecycle.ViewModel
import com.michaldrabik.repository.images.MovieImagesProvider
import com.michaldrabik.ui_base.utilities.extensions.rethrowCancellation
import com.michaldrabik.ui_base.viewmodel.ChannelsDelegate
import com.michaldrabik.ui_base.viewmodel.DefaultChannelsDelegate
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_movie.cases.MovieDetailsMyMoviesCase
import com.michaldrabik.ui_movie.cases.MovieDetailsRelatedCase
import com.michaldrabik.ui_movie.related.recycler.RelatedListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class MovieDetailsRelatedViewModel @Inject constructor(
  private val relatedCase: MovieDetailsRelatedCase,
  private val myMoviesCase: MovieDetailsMyMoviesCase,
  private val imagesProvider: MovieImagesProvider,
) : ViewModel(), ChannelsDelegate by DefaultChannelsDelegate() {

  private val relatedState = MutableStateFlow<List<RelatedListItem>?>(null)

  private suspend fun loadRelatedMovies(movie: Movie) {
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
      rethrowCancellation(error)
    }
  }
}
