package com.michaldrabik.showly2.ui.main

import com.michaldrabik.ui_base.UiModel

data class MainUiModel(
  val isInitialRun: Boolean? = null,
  val showWhatsNew: Boolean? = null,
  val showRateApp: Boolean? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as MainUiModel).copy(
      isInitialRun = newModel.isInitialRun ?: isInitialRun,
      showWhatsNew = newModel.showWhatsNew ?: showWhatsNew,
      showRateApp = newModel.showRateApp ?: showRateApp
    )
}
