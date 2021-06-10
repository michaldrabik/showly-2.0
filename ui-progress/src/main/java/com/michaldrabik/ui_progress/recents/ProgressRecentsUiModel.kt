package com.michaldrabik.ui_progress.recents

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_progress.ProgressItem

data class ProgressRecentsUiModel(
  val items: List<ProgressItem>? = null,
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as ProgressRecentsUiModel).copy(
      items = newModel.items?.toList() ?: items
    )
}
