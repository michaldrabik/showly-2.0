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

  fun setTheme(theme: AppTheme) = settingsRepository.setTheme(theme.code)

  fun getTheme() = AppTheme.fromCode(settingsRepository.getTheme())

  fun setWidgetsTheme(theme: AppTheme, context: Context) {
    settingsRepository.setWidgetsTheme(theme.code)
    (context.applicationContext as WidgetsProvider).run {
      requestShowsWidgetsUpdate()
      requestMoviesWidgetsUpdate()
    }
  }

  fun getWidgetsTheme() = AppTheme.fromCode(settingsRepository.getWidgetsTheme())
}
