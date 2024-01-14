package com.michaldrabik.ui_discover.filters.feed

import com.michaldrabik.ui_model.DiscoverSortOrder

internal data class DiscoverFiltersFeedUiState(
  val feedOrder: DiscoverSortOrder? = null,
  val isLoading: Boolean? = null,
)
