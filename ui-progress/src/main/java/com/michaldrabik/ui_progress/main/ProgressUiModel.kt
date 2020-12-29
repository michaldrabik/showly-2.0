package com.michaldrabik.ui_progress.main

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_progress.ProgressItem

data class ProgressUiModel(
  val items: List<ProgressItem>? = null,
  val isSearching: Boolean? = null,
  val resetScroll: ActionEvent<Boolean>? = null,
  val sortOrder: SortOrder? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as ProgressUiModel).copy(
      items = newModel.items?.toList() ?: items,
      isSearching = newModel.isSearching ?: isSearching,
      resetScroll = newModel.resetScroll ?: resetScroll,
      sortOrder = newModel.sortOrder ?: sortOrder
    )
}
