package com.michaldrabik.showly2.ui.settings

import com.michaldrabik.showly2.model.Settings
import com.michaldrabik.showly2.ui.common.UiModel

data class SettingsUiModel(
  val settings: Settings? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as SettingsUiModel).copy(
      settings = newModel.settings ?: settings
    )
}