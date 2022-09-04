package com.michaldrabik.ui_discover.filters.genres

import com.michaldrabik.ui_model.Genre

internal data class DiscoverFiltersGenresUiState(
  val genres: List<Genre>? = null,
  val isLoading: Boolean? = null,
)
