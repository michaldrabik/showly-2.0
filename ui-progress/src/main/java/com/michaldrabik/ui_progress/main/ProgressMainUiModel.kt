package com.michaldrabik.ui_progress.main

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_progress.calendar.helpers.CalendarMode

data class ProgressMainUiModel(
  val timestamp: Long? = null,
  val searchQuery: String? = null,
  val calendarMode: CalendarMode? = null,
  val resetScroll: ActionEvent<Boolean>? = null,
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as ProgressMainUiModel).copy(
      timestamp = newModel.timestamp ?: timestamp,
      searchQuery = newModel.searchQuery ?: searchQuery,
      resetScroll = newModel.resetScroll ?: resetScroll,
      calendarMode = newModel.calendarMode ?: calendarMode
    )
}
