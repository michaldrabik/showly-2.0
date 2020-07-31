package com.michaldrabik.showly2.ui.main

import com.michaldrabik.showly2.ui.common.UiModel

data class MainUiModel(
  val isInitialRun: Boolean? = null,
  val showWhatsNew: Boolean? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as MainUiModel).copy(
      isInitialRun = newModel.isInitialRun ?: isInitialRun,
      showWhatsNew = newModel.showWhatsNew ?: showWhatsNew
    )
}
