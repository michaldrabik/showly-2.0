package com.michaldrabik.ui_settings

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_model.Settings
import com.michaldrabik.ui_settings.helpers.AppLanguage

data class SettingsUiModel(
  val language: AppLanguage? = null,
  val settings: Settings? = null,
  val isSignedInTrakt: Boolean? = null,
  val traktUsername: String? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as SettingsUiModel).copy(
      language = newModel.language ?: language,
      settings = newModel.settings ?: settings,
      isSignedInTrakt = newModel.isSignedInTrakt ?: isSignedInTrakt,
      traktUsername = newModel.traktUsername ?: traktUsername
    )
}
