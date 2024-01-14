package com.michaldrabik.ui_discover.filters.networks

import com.michaldrabik.ui_model.Network

internal data class DiscoverFiltersNetworksUiState(
  val networks: List<Network>? = null,
  val isLoading: Boolean? = null,
)
