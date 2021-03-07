package com.michaldrabik.ui_lists.create

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_base.utilities.ActionEvent

data class CreateListUiModel(
  val isLoading: Boolean? = null,
  val successEvent: ActionEvent<Boolean>? = null,
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as CreateListUiModel).copy(
      isLoading = newModel.isLoading ?: isLoading,
      successEvent = newModel.successEvent ?: successEvent
    )
}
