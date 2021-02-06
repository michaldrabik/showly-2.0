package com.michaldrabik.ui_settings

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_base.common.AppCountry
import com.michaldrabik.ui_base.dates.AppDateFormat
import com.michaldrabik.ui_model.Settings
import com.michaldrabik.ui_settings.helpers.AppLanguage
import com.michaldrabik.ui_settings.helpers.AppTheme
import com.michaldrabik.ui_settings.helpers.WidgetTransparency

data class SettingsUiModel(
  val language: AppLanguage? = null,
  val theme: AppTheme? = null,
  val themeWidgets: AppTheme? = null,
  val widgetsTransparency: WidgetTransparency? = null,
  val country: AppCountry? = null,
  val dateFormat: AppDateFormat? = null,
  val settings: Settings? = null,
  val isSignedInTrakt: Boolean? = null,
  val isSigningIn: Boolean? = null,
  val isPremium: Boolean? = null,
  val traktUsername: String? = null,
  val userId: String? = null,
  val moviesEnabled: Boolean? = null,
  val restartApp: Boolean? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as SettingsUiModel).copy(
      language = newModel.language ?: language,
      country = newModel.country ?: country,
      theme = newModel.theme ?: theme,
      themeWidgets = newModel.themeWidgets ?: themeWidgets,
      widgetsTransparency = newModel.widgetsTransparency ?: widgetsTransparency,
      dateFormat = newModel.dateFormat ?: dateFormat,
      settings = newModel.settings ?: settings,
      isSignedInTrakt = newModel.isSignedInTrakt ?: isSignedInTrakt,
      isSigningIn = newModel.isSigningIn ?: isSigningIn,
      isPremium = newModel.isPremium ?: isPremium,
      traktUsername = newModel.traktUsername ?: traktUsername,
      userId = newModel.userId ?: userId,
      moviesEnabled = newModel.moviesEnabled ?: moviesEnabled,
      restartApp = newModel.restartApp ?: restartApp
    )
}
