// ktlint-disable filename
package com.michaldrabik.ui_discover_movies.filters.genres

import com.michaldrabik.ui_base.utilities.events.Event

internal sealed class DiscoverMoviesFiltersGenresUiEvent<T>(action: T) : Event<T>(action) {
  object ApplyFilters : DiscoverMoviesFiltersGenresUiEvent<Unit>(Unit)
  object CloseFilters : DiscoverMoviesFiltersGenresUiEvent<Unit>(Unit)
}
