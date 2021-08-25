package com.michaldrabik.ui_progress_movies.progress

import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_progress_movies.progress.recycler.ProgressMovieListItem

data class ProgressMoviesUiState(
  val items: List<ProgressMovieListItem.MovieItem>? = null,
  val scrollReset: ActionEvent<Boolean>? = null,
  val sortOrder: ActionEvent<SortOrder>? = null,
)
