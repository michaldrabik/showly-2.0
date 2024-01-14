package com.michaldrabik.ui_progress_movies.main

import com.michaldrabik.ui_model.CalendarMode

data class ProgressMoviesMainUiState(
  val timestamp: Long? = null,
  val searchQuery: String? = null,
  val calendarMode: CalendarMode? = null,
  val isSyncing: Boolean = false,
)
