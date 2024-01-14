package com.michaldrabik.ui_discover_movies.filters.feed

import com.michaldrabik.ui_model.DiscoverSortOrder

internal data class DiscoverMoviesFiltersFeedUiState(
  val feedOrder: DiscoverSortOrder? = null,
  val isLoading: Boolean? = null,
)
