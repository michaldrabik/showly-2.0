// ktlint-disable filename
package com.michaldrabik.ui_discover.filters

import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_model.DiscoverFilters

internal sealed class DiscoverFiltersUiEvent<T>(action: T) : Event<T>(action) {

  data class ApplyFilters(
    val filters: DiscoverFilters,
  ) : DiscoverFiltersUiEvent<DiscoverFilters>(filters)

  object CloseFilters : DiscoverFiltersUiEvent<Unit>(Unit)
}
