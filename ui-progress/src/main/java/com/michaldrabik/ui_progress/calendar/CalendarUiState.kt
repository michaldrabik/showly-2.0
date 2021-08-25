package com.michaldrabik.ui_progress.calendar

import com.michaldrabik.ui_progress.calendar.helpers.CalendarMode
import com.michaldrabik.ui_progress.calendar.recycler.CalendarListItem

data class CalendarUiState(
  val items: List<CalendarListItem>? = null,
  val mode: CalendarMode = CalendarMode.PRESENT_FUTURE
)
