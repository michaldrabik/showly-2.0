package com.michaldrabik.ui_progress.calendar

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_progress.ProgressItem

data class CalendarUiModel(
  val items: List<ProgressItem>? = null,
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as CalendarUiModel).copy(
      items = newModel.items?.toList() ?: items
    )
}
