// ktlint-disable filename
package com.michaldrabik.ui_discover.filters.genres

import com.michaldrabik.ui_base.utilities.events.Event

internal sealed class DiscoverFiltersGenresUiEvent<T>(action: T) : Event<T>(action) {
  object ApplyFilters : DiscoverFiltersGenresUiEvent<Unit>(Unit)
  object CloseFilters : DiscoverFiltersGenresUiEvent<Unit>(Unit)
}
