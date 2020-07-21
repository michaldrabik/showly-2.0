package com.michaldrabik.showly2.model

data class DiscoverFilters(
  val sortOrder: DiscoverSortOrder = DiscoverSortOrder.HOT,
  val showAnticipated: Boolean = true,
  val genres: List<Genre> = emptyList()
)
