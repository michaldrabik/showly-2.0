package com.michaldrabik.ui_progress.progress

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_progress.progress.recycler.ProgressListItem

data class ProgressUiModel(
  val items: List<ProgressListItem>? = null,
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as ProgressUiModel).copy(
      items = newModel.items?.toList() ?: items,
    )
}
