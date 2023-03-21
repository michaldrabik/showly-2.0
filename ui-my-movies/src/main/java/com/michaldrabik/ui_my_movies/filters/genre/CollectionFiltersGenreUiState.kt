package com.michaldrabik.ui_my_movies.filters.genre

import com.michaldrabik.ui_model.Genre

internal data class CollectionFiltersGenreUiState(
  val genres: List<Genre>? = null,
  val isLoading: Boolean? = null,
)
