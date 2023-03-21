// ktlint-disable filename
package com.michaldrabik.ui_my_movies.filters

import com.michaldrabik.ui_base.utilities.events.Event

internal sealed class CollectionFiltersUiEvent<T>(action: T) : Event<T>(action) {
  object ApplyFilters : CollectionFiltersUiEvent<Unit>(Unit)
  object CloseFilters : CollectionFiltersUiEvent<Unit>(Unit)
}
