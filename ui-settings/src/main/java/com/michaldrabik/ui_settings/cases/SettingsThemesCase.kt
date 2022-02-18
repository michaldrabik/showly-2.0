package com.michaldrabik.ui_settings.cases

import android.content.Context
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.common.WidgetsProvider
import com.michaldrabik.ui_settings.helpers.AppTheme
import com.michaldrabik.ui_settings.helpers.WidgetTransparency
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class SettingsThemesCase @Inject constructor(
  private val settingsRepository: SettingsRepository
) {

  fun setTheme(theme: AppTheme) {
    settingsRepository.theme = theme.code
  }

  fun getTheme() = AppTheme.fromCode(settingsRepository.theme)

  fun setWidgetsTheme(theme: AppTheme, context: Context) {
    settingsRepository.widgets.widgetsTheme = theme.code
    reloadWidgets(context)
  }

  fun setWidgetsTransparency(transparency: WidgetTransparency, context: Context) {
    settingsRepository.widgets.widgetsTransparency = transparency.value
    reloadWidgets(context)
  }

  private fun reloadWidgets(context: Context) {
    (context.applicationContext as WidgetsProvider).run {
      requestShowsWidgetsUpdate()
      requestMoviesWidgetsUpdate()
    }
  }

  fun getWidgetsTheme() = AppTheme.fromCode(settingsRepository.widgets.widgetsTheme)

  fun getWidgetsTransparency() = WidgetTransparency.fromValue(settingsRepository.widgets.widgetsTransparency)
}
