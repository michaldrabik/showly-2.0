// ktlint-disable filename
package com.michaldrabik.ui_discover_movies.filters.feed

import com.michaldrabik.ui_base.utilities.events.Event

internal sealed class DiscoverMoviesFiltersFeedUiEvent<T>(action: T) : Event<T>(action) {
  object ApplyFilters : DiscoverMoviesFiltersFeedUiEvent<Unit>(Unit)
  object CloseFilters : DiscoverMoviesFiltersFeedUiEvent<Unit>(Unit)
}
