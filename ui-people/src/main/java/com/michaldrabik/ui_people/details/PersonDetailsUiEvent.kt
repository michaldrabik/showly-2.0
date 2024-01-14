// ktlint-disable filename
package com.michaldrabik.ui_people.details

import com.michaldrabik.ui_base.utilities.events.Event

internal sealed class PersonDetailsUiEvent<T>(action: T) : Event<T>(action) {
  data class ScrollToPosition(
    val position: Int,
    val isSheetExpanded: Boolean,
    val isUpButtonVisible: Boolean
  ) : PersonDetailsUiEvent<Int>(position)
}
