package com.michaldrabik.ui_settings

import com.michaldrabik.ui_base.common.AppCountry
import com.michaldrabik.ui_base.dates.AppDateFormat
import com.michaldrabik.ui_model.ProgressNextEpisodeType
import com.michaldrabik.ui_model.Settings
import com.michaldrabik.ui_settings.helpers.AppLanguage
import com.michaldrabik.ui_settings.helpers.AppTheme
import com.michaldrabik.ui_settings.helpers.WidgetTransparency

data class SettingsUiState(
  val language: AppLanguage = AppLanguage.ENGLISH,
  val theme: AppTheme = AppTheme.DARK,
  val themeWidgets: AppTheme? = AppTheme.DARK,
  val widgetsTransparency: WidgetTransparency = WidgetTransparency.SOLID,
  val country: AppCountry? = null,
  val dateFormat: AppDateFormat? = null,
  val settings: Settings? = null,
  val isSignedInTrakt: Boolean = false,
  val isSigningIn: Boolean = false,
  val isPremium: Boolean = false,
  val traktUsername: String = "",
  val userId: String = "",
  val moviesEnabled: Boolean = true,
  val newsEnabled: Boolean = false,
  val streamingsEnabled: Boolean = true,
  val restartApp: Boolean = false,
  val progressNextType: ProgressNextEpisodeType? = null,
)
