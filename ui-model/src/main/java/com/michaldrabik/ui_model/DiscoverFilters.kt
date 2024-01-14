package com.michaldrabik.ui_model

data class DiscoverFilters(
  val feedOrder: DiscoverSortOrder = DiscoverSortOrder.HOT,
  val hideAnticipated: Boolean = true,
  val hideCollection: Boolean = false,
  val genres: List<Genre> = emptyList(),
  val networks: List<Network> = emptyList(),
)
