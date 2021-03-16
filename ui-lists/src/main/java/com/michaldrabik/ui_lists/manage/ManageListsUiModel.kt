package com.michaldrabik.ui_lists.manage

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_model.CustomList

data class ManageListsUiModel(
  val isLoading: Boolean? = null,
  val listUpdatedEvent: ActionEvent<CustomList>? = null,
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as ManageListsUiModel).copy(
      isLoading = newModel.isLoading ?: isLoading,
      listUpdatedEvent = newModel.listUpdatedEvent ?: listUpdatedEvent
    )
}
