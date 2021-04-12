package com.michaldrabik.ui_lists.manage

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_lists.manage.recycler.ManageListsItem

data class ManageListsUiModel(
  val items: List<ManageListsItem>? = null,
  val isLoading: Boolean? = null,
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as ManageListsUiModel).copy(
      items = newModel.items?.toList() ?: items,
      isLoading = newModel.isLoading ?: isLoading,
    )
}
