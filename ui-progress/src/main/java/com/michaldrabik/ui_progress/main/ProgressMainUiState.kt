package com.michaldrabik.ui_progress.main

import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_model.CalendarMode

data class ProgressMainUiState(
  val timestamp: Long? = null,
  val searchQuery: String? = null,
  val calendarMode: CalendarMode? = null,
  val resetScroll: Event<Boolean>? = null,
  val isSyncing: Boolean = false,
)
