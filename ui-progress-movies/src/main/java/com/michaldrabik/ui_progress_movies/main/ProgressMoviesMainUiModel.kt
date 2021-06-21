package com.michaldrabik.ui_progress_movies.main

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_progress_movies.calendar.helpers.CalendarMode

data class ProgressMoviesMainUiModel(
  val timestamp: Long? = null,
  val searchQuery: String? = null,
  val calendarMode: CalendarMode? = null,
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as ProgressMoviesMainUiModel).copy(
      timestamp = newModel.timestamp ?: timestamp,
      searchQuery = newModel.searchQuery ?: searchQuery,
      calendarMode = newModel.calendarMode ?: calendarMode
    )
}
