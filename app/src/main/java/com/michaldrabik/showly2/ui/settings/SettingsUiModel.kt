package com.michaldrabik.showly2.ui.settings

import com.michaldrabik.showly2.ui.common.UiModel
import com.michaldrabik.ui_model.Settings

data class SettingsUiModel(
  val settings: Settings? = null,
  val isSignedInTrakt: Boolean? = null,
  val traktUsername: String? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as SettingsUiModel).copy(
      settings = newModel.settings ?: settings,
      isSignedInTrakt = newModel.isSignedInTrakt ?: isSignedInTrakt,
      traktUsername = newModel.traktUsername ?: traktUsername
    )
}
