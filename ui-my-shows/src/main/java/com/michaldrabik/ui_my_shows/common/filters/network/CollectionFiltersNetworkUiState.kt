package com.michaldrabik.ui_my_shows.common.filters.network

import com.michaldrabik.ui_model.Network

internal data class CollectionFiltersNetworkUiState(
  val networks: List<Network>? = null,
  val isLoading: Boolean? = null,
)
