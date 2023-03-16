// ktlint-disable filename
package com.michaldrabik.ui_my_shows.common.filters

import com.michaldrabik.ui_base.utilities.events.Event

internal sealed class CollectionFiltersNetworkUiEvent<T>(action: T) : Event<T>(action) {
  object ApplyFilters : CollectionFiltersNetworkUiEvent<Unit>(Unit)
  object CloseFilters : CollectionFiltersNetworkUiEvent<Unit>(Unit)
}
