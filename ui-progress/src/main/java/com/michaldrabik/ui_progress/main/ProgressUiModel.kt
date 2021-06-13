package com.michaldrabik.ui_progress.main

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_progress.ProgressItem

data class ProgressUiModel(
  val items: List<ProgressItem>? = null,
  val searchQuery: String? = null,
  val resetScroll: ActionEvent<Boolean>? = null,
  val sortOrder: SortOrder? = null,
  val isUpcomingEnabled: Boolean? = null,
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as ProgressUiModel).copy(
      items = newModel.items?.toList() ?: items,
      searchQuery = newModel.searchQuery ?: searchQuery,
      isUpcomingEnabled = newModel.isUpcomingEnabled ?: isUpcomingEnabled,
      resetScroll = newModel.resetScroll ?: resetScroll,
      sortOrder = newModel.sortOrder ?: sortOrder
    )
}
