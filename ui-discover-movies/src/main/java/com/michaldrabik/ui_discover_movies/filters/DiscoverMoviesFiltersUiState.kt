package com.michaldrabik.ui_discover_movies.filters

import com.michaldrabik.ui_model.DiscoverFilters

internal data class DiscoverMoviesFiltersUiState(
  val filters: DiscoverFilters? = null,
  val isLoading: Boolean? = null,
)
