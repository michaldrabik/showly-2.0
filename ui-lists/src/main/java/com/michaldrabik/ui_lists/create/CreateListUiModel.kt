package com.michaldrabik.ui_lists.create

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_model.CustomList

data class CreateListUiModel(
  val isLoading: Boolean? = null,
  val listCreatedEvent: ActionEvent<CustomList>? = null,
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as CreateListUiModel).copy(
      isLoading = newModel.isLoading ?: isLoading,
      listCreatedEvent = newModel.listCreatedEvent ?: listCreatedEvent
    )
}
