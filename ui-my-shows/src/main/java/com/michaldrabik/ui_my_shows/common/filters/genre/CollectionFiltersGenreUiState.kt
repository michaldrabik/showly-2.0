package com.michaldrabik.ui_my_shows.common.filters.genre

import com.michaldrabik.ui_model.Genre

internal data class CollectionFiltersGenreUiState(
  val genres: List<Genre>? = null,
  val isLoading: Boolean? = null,
)
