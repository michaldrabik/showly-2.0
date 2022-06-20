package com.michaldrabik.ui_discover.filters

import com.michaldrabik.ui_model.DiscoverFilters

internal data class DiscoverFiltersUiState(
  val filters: DiscoverFilters? = null,
  val isLoading: Boolean? = null,
)
