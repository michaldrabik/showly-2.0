package com.michaldrabik.ui_discover_movies.filters.genres

import com.michaldrabik.ui_model.Genre

internal data class DiscoverMoviesFiltersGenresUiState(
  val genres: List<Genre>? = null,
  val isLoading: Boolean? = null,
)
