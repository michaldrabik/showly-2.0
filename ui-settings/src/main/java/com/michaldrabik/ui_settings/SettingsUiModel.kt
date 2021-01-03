package com.michaldrabik.ui_settings

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_base.common.AppCountry
import com.michaldrabik.ui_model.Settings
import com.michaldrabik.ui_settings.helpers.AppLanguage

data class SettingsUiModel(
  val language: AppLanguage? = null,
  val country: AppCountry? = null,
  val settings: Settings? = null,
  val isSignedInTrakt: Boolean? = null,
  val isSigningIn: Boolean? = null,
  val traktUsername: String? = null,
  val moviesEnabled: Boolean? = null,
  val restartApp: Boolean? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as SettingsUiModel).copy(
      language = newModel.language ?: language,
      country = newModel.country ?: country,
      settings = newModel.settings ?: settings,
      isSignedInTrakt = newModel.isSignedInTrakt ?: isSignedInTrakt,
      isSigningIn = newModel.isSigningIn ?: isSigningIn,
      traktUsername = newModel.traktUsername ?: traktUsername,
      moviesEnabled = newModel.moviesEnabled ?: moviesEnabled,
      restartApp = newModel.restartApp ?: restartApp
    )
}
