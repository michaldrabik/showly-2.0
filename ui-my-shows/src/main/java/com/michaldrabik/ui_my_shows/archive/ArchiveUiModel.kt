package com.michaldrabik.ui_my_shows.archive

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_my_shows.archive.recycler.ArchiveListItem

data class ArchiveUiModel(
  val items: List<ArchiveListItem>? = null,
  val scrollToTop: ActionEvent<Boolean>? = null,
  val sortOrder: ActionEvent<SortOrder>? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as ArchiveUiModel).copy(
      items = newModel.items?.toList() ?: items,
      scrollToTop = newModel.scrollToTop ?: scrollToTop,
      sortOrder = newModel.sortOrder ?: sortOrder
    )
}
