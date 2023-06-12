package com.michaldrabik.ui_movie.sections.ratings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.utilities.extensions.rethrowCancellation
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Ratings
import com.michaldrabik.ui_movie.sections.ratings.cases.MovieDetailsRatingCase
import com.michaldrabik.ui_movie.sections.ratings.cases.MovieDetailsRatingSpoilersCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MovieDetailsRatingsViewModel @Inject constructor(
  private val ratingsCase: MovieDetailsRatingCase,
  private val ratingsSpoilersCase: MovieDetailsRatingSpoilersCase,
) : ViewModel() {

  private lateinit var movie: Movie

  private val movieState = MutableStateFlow<Movie?>(null)
  private val ratingsState = MutableStateFlow<Ratings?>(null)
  private val isRefreshingRatingsState = MutableStateFlow(false)

  fun loadRatings(movie: Movie) {
    if (this::movie.isInitialized) return
    this.movie = movie

    viewModelScope.launch {
      movieState.value = movie

      val traktRatings = Ratings(
        trakt = Ratings.Value(String.format(Locale.ENGLISH, "%.1f", movie.rating), false),
        imdb = Ratings.Value(null, true),
        metascore = Ratings.Value(null, true),
        rottenTomatoes = Ratings.Value(null, true)
      )

      try {
        ratingsState.value = ratingsSpoilersCase.hideSpoilerRatings(movie, traktRatings)
        val ratings = ratingsCase.loadExternalRatings(movie)
        ratingsState.value = ratingsSpoilersCase.hideSpoilerRatings(movie, ratings)
      } catch (error: Throwable) {
        ratingsState.value = ratingsSpoilersCase.hideSpoilerRatings(movie, traktRatings)
        rethrowCancellation(error)
      }
    }
  }

  fun refreshRatings() {
    val movie = movieState.value
    val ratings = ratingsState.value
    viewModelScope.launch {
      if (movie != null && ratings != null) {
        isRefreshingRatingsState.value = true
        ratingsState.value = ratingsSpoilersCase.hideSpoilerRatings(movie, ratings)
      }
    }
  }

  val uiState = combine(
    ratingsState,
    movieState,
    isRefreshingRatingsState
  ) { s1, s2, s3 ->
    MovieDetailsRatingsUiState(
      ratings = s1,
      movie = s2,
      isRefreshingRatings = s3
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = MovieDetailsRatingsUiState()
  )
}
