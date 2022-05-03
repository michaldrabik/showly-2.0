// ktlint-disable filename
package com.michaldrabik.ui_movie

import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_model.Movie

data class MovieLoadedEvent(val movie: Movie) : Event<Movie>(movie)
