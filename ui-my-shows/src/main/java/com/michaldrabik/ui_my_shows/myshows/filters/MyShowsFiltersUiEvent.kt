// ktlint-disable filename
package com.michaldrabik.ui_my_shows.myshows.filters

import com.michaldrabik.ui_base.utilities.events.Event

internal sealed class MyShowsFiltersUiEvent<T>(action: T) : Event<T>(action) {
  object ApplyFilters : MyShowsFiltersUiEvent<Unit>(Unit)
  object CloseFilters : MyShowsFiltersUiEvent<Unit>(Unit)
}
