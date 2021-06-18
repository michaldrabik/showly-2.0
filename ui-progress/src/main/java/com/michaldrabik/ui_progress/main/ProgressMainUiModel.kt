package com.michaldrabik.ui_progress.main

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_base.utilities.ActionEvent

data class ProgressMainUiModel(
  val resetScroll: ActionEvent<Boolean>? = null,
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as ProgressMainUiModel).copy(
      resetScroll = newModel.resetScroll ?: resetScroll
    )
}
