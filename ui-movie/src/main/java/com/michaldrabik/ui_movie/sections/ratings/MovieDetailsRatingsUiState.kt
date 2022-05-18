package com.michaldrabik.ui_movie.sections.ratings

import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Ratings

data class MovieDetailsRatingsUiState(
  val movie: Movie? = null,
  val ratings: Ratings? = null
)
