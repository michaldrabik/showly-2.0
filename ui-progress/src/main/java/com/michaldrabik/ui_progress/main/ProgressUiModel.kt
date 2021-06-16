package com.michaldrabik.ui_progress.main

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_progress.ProgressItem
import com.michaldrabik.ui_progress.calendar.helpers.CalendarMode

data class ProgressUiModel(
  val items: List<ProgressItem>? = null,
  val searchQuery: String? = null,
  val resetScroll: ActionEvent<Boolean>? = null,
  val calendarMode: CalendarMode? = null,
  val sortOrder: SortOrder? = null,
  val isUpcomingEnabled: Boolean? = null,
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as ProgressUiModel).copy(
      items = newModel.items?.toList() ?: items,
      searchQuery = newModel.searchQuery ?: searchQuery,
      calendarMode = newModel.calendarMode ?: calendarMode,
      isUpcomingEnabled = newModel.isUpcomingEnabled ?: isUpcomingEnabled,
      resetScroll = newModel.resetScroll ?: resetScroll,
      sortOrder = newModel.sortOrder ?: sortOrder
    )
}
