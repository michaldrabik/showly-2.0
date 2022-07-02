// ktlint-disable filename
package com.michaldrabik.ui_discover_movies.filters

import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_model.DiscoverFilters

internal sealed class DiscoverMoviesFiltersUiEvent<T>(action: T) : Event<T>(action) {

  data class ApplyFilters(
    val filters: DiscoverFilters,
  ) : DiscoverMoviesFiltersUiEvent<DiscoverFilters>(filters)

  object CloseFilters : DiscoverMoviesFiltersUiEvent<Unit>(Unit)
}
