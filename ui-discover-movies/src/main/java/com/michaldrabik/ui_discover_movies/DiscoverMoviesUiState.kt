package com.michaldrabik.ui_discover_movies

import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_discover_movies.recycler.DiscoverMovieListItem
import com.michaldrabik.ui_model.DiscoverFilters

data class DiscoverMoviesUiState(
  val items: List<DiscoverMovieListItem>? = null,
  val isLoading: Boolean? = null,
  var filters: DiscoverFilters? = null,
  var resetScroll: ActionEvent<Boolean>? = null,
)
