package com.michaldrabik.ui_progress_movies.progress

import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_progress_movies.progress.recycler.ProgressMovieListItem

data class ProgressMoviesUiState(
  val items: List<ProgressMovieListItem>? = null,
  val scrollReset: Event<Boolean>? = null,
  val sortOrder: Event<Pair<SortOrder, SortType>>? = null,
  val isOverScrollEnabled: Boolean = false
)
