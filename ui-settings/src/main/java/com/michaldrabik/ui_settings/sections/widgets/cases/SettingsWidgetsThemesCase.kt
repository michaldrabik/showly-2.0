package com.michaldrabik.ui_settings.sections.widgets.cases

import android.content.Context
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.common.WidgetsProvider
import com.michaldrabik.ui_settings.helpers.AppTheme
import com.michaldrabik.ui_settings.helpers.WidgetTransparency
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class SettingsWidgetsThemesCase @Inject constructor(
  private val settingsRepository: SettingsRepository,
) {

  fun setWidgetsTheme(theme: AppTheme, context: Context) {
    settingsRepository.widgets.widgetsTheme = theme.code
    reloadWidgets(context)
  }

  fun setWidgetsTransparency(transparency: WidgetTransparency, context: Context) {
    settingsRepository.widgets.widgetsTransparency = transparency.value
    reloadWidgets(context)
  }

  fun getWidgetsTheme() = AppTheme.fromCode(settingsRepository.widgets.widgetsTheme)

  fun getWidgetsTransparency() = WidgetTransparency.fromValue(settingsRepository.widgets.widgetsTransparency)

  private fun reloadWidgets(context: Context) {
    (context.applicationContext as WidgetsProvider).run {
      requestShowsWidgetsUpdate()
      requestMoviesWidgetsUpdate()
    }
  }
}
