// ktlint-disable filename
package com.michaldrabik.ui_discover.filters.networks

import com.michaldrabik.ui_base.utilities.events.Event

internal sealed class DiscoverFiltersNetworksUiEvent<T>(action: T) : Event<T>(action) {
  object ApplyFilters : DiscoverFiltersNetworksUiEvent<Unit>(Unit)
  object CloseFilters : DiscoverFiltersNetworksUiEvent<Unit>(Unit)
}
