// ktlint-disable filename
package com.michaldrabik.ui_progress_movies.main

import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_model.Movie

data class MovieCheckActionUiEvent(val movie: Movie) : Event<Movie>(movie)

object RequestWidgetsUpdate : Event<Unit>(Unit)
