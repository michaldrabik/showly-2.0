package com.michaldrabik.ui_progress.calendar

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_progress.ProgressItem

data class ProgressCalendarUiModel(
  val items: List<ProgressItem>? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as ProgressCalendarUiModel).copy(
      items = newModel.items?.toList() ?: items
    )
}
