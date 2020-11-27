package com.michaldrabik.ui_discover_movies

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_discover_movies.recycler.DiscoverMovieListItem
import com.michaldrabik.ui_model.DiscoverFilters

data class DiscoverMoviesUiModel(
  val movies: List<DiscoverMovieListItem>? = null,
  val showLoading: Boolean? = null,
  var filters: DiscoverFilters? = null,
  var resetScroll: Boolean? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as DiscoverMoviesUiModel).copy(
      movies = newModel.movies ?: movies,
      showLoading = newModel.showLoading ?: showLoading,
      filters = newModel.filters ?: filters,
      resetScroll = newModel.resetScroll ?: resetScroll
    )
}
