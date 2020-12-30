package com.michaldrabik.ui_settings

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_model.Settings
import com.michaldrabik.ui_settings.helpers.AppLanguage
import com.michaldrabik.ui_settings.helpers.AppTheme

data class SettingsUiModel(
  val language: AppLanguage? = null,
  val theme: AppTheme? = null,
  val themeWidgets: AppTheme? = null,
  val settings: Settings? = null,
  val isSignedInTrakt: Boolean? = null,
  val traktUsername: String? = null,
  val moviesEnabled: Boolean? = null,
  val restartApp: Boolean? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as SettingsUiModel).copy(
      language = newModel.language ?: language,
      theme = newModel.theme ?: theme,
      themeWidgets = newModel.themeWidgets ?: themeWidgets,
      settings = newModel.settings ?: settings,
      isSignedInTrakt = newModel.isSignedInTrakt ?: isSignedInTrakt,
      traktUsername = newModel.traktUsername ?: traktUsername,
      moviesEnabled = newModel.moviesEnabled ?: moviesEnabled,
      restartApp = newModel.restartApp ?: restartApp
    )
}
