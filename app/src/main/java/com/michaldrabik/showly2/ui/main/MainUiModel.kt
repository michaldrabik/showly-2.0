package com.michaldrabik.showly2.ui.main

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_settings.helpers.AppLanguage

data class MainUiModel(
  val isInitialRun: ActionEvent<Boolean>? = null,
  val showWhatsNew: ActionEvent<Boolean>? = null,
  val initialLanguage: ActionEvent<AppLanguage>? = null,
  val showRateApp: ActionEvent<Boolean>? = null,
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as MainUiModel).copy(
      isInitialRun = newModel.isInitialRun ?: isInitialRun,
      showWhatsNew = newModel.showWhatsNew ?: showWhatsNew,
      initialLanguage = newModel.initialLanguage ?: initialLanguage,
      showRateApp = newModel.showRateApp ?: showRateApp
    )
}
