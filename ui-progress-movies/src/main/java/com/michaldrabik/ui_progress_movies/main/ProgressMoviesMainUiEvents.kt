// ktlint-disable filename
package com.michaldrabik.ui_progress_movies.main

import com.michaldrabik.ui_base.utilities.Event
import com.michaldrabik.ui_model.Movie

data class MovieCheckActionUiEvent(
  val movie: Movie,
  val isQuickRate: Boolean,
) : Event<Movie>(movie)
