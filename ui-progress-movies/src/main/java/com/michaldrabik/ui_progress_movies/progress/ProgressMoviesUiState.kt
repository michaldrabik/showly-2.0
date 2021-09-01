package com.michaldrabik.ui_progress_movies.progress

import com.michaldrabik.ui_base.utilities.Event
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_progress_movies.progress.recycler.ProgressMovieListItem

data class ProgressMoviesUiState(
  val items: List<ProgressMovieListItem.MovieItem>? = null,
  val scrollReset: Event<Boolean>? = null,
  val sortOrder: Event<SortOrder>? = null,
)
