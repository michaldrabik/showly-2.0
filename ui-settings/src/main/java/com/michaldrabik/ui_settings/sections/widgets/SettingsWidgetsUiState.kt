package com.michaldrabik.ui_settings.sections.widgets

import com.michaldrabik.ui_model.Settings
import com.michaldrabik.ui_settings.helpers.AppTheme
import com.michaldrabik.ui_settings.helpers.WidgetTransparency

data class SettingsWidgetsUiState(
  val settings: Settings? = null,
  val themeWidgets: AppTheme? = AppTheme.DARK,
  val widgetsTransparency: WidgetTransparency = WidgetTransparency.SOLID,
  val isPremium: Boolean = false,
)
