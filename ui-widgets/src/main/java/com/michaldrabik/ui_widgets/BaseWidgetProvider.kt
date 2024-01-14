package com.michaldrabik.ui_widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_model.Settings
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

abstract class BaseWidgetProvider : AppWidgetProvider() {

  companion object {
    const val ACTION_CLICK = "ACTION_CLICK"
    const val EXTRA_MODE_CLICK = "EXTRA_MODE_CLICK"
    const val EXTRA_SHOW_ID = "EXTRA_SHOW_ID"
    const val EXTRA_MOVIE_ID = "EXTRA_MOVIE_ID"
  }

  @Inject lateinit var settingsRepository: SettingsRepository
  protected lateinit var settings: Settings

  abstract fun getLayoutResId(): Int

  protected fun getBackgroundResId() =
    when (settingsRepository.widgets.widgetsTransparency) {
      75 -> R.drawable.bg_widget_75
      50 -> R.drawable.bg_widget_50
      25 -> R.drawable.bg_widget_25
      0 -> R.drawable.bg_widget_0
      else -> R.drawable.bg_widget
    }

  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray?,
  ) {
    requireSettings()
    super.onUpdate(context, appWidgetManager, appWidgetIds)
  }

  private fun requireSettings() {
    if (!this::settings.isInitialized) {
      settings = runBlocking { settingsRepository.load() }
    }
  }
}
