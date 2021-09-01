package com.michaldrabik.ui_progress.main

import com.michaldrabik.ui_base.utilities.Event
import com.michaldrabik.ui_progress.calendar.helpers.CalendarMode

data class ProgressMainUiState(
  val timestamp: Long? = null,
  val searchQuery: String? = null,
  val calendarMode: CalendarMode? = null,
  val resetScroll: Event<Boolean>? = null,
)
