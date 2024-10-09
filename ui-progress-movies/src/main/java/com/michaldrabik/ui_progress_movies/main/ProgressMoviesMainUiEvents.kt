@file:Suppress("ktlint:standard:filename")

package com.michaldrabik.ui_progress_movies.main

import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.ProgressDateSelectionType

data class MovieCheckActionUiEvent(
  val movie: Movie,
  val dateSelectionType: ProgressDateSelectionType,
) : Event<Movie>(movie)

object RequestWidgetsUpdate : Event<Unit>(Unit)
