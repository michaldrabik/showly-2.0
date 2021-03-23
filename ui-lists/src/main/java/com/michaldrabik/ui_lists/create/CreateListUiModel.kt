package com.michaldrabik.ui_lists.create

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_model.CustomList

data class CreateListUiModel(
  val listDetails: CustomList? = null,
  val isLoading: Boolean? = null,
  val listUpdatedEvent: ActionEvent<CustomList>? = null,
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as CreateListUiModel).copy(
      isLoading = newModel.isLoading ?: isLoading,
      listDetails = newModel.listDetails ?: listDetails,
      listUpdatedEvent = newModel.listUpdatedEvent ?: listUpdatedEvent
    )
}
