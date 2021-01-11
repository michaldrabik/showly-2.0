package com.michaldrabik.ui_settings.cases

import android.content.Context
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.ui_base.common.WidgetsProvider
import com.michaldrabik.ui_repository.SettingsRepository
import com.michaldrabik.ui_settings.helpers.AppTheme
import javax.inject.Inject

@AppScope
class SettingsThemesCase @Inject constructor(
  private val settingsRepository: SettingsRepository
) {

  fun setTheme(theme: AppTheme) {
    settingsRepository.theme = theme.code
  }

  fun getTheme() = AppTheme.fromCode(settingsRepository.theme)

  fun setWidgetsTheme(theme: AppTheme, context: Context) {
    settingsRepository.widgetsTheme = theme.code
    (context.applicationContext as WidgetsProvider).run {
      requestShowsWidgetsUpdate()
      requestMoviesWidgetsUpdate()
    }
  }

  fun getWidgetsTheme() = AppTheme.fromCode(settingsRepository.widgetsTheme)
}
