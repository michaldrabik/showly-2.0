package com.michaldrabik.ui_widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent

abstract class BaseWidgetProvider : AppWidgetProvider() {

  companion object {
    const val ACTION_LIST_CLICK = "ACTION_LIST_CLICK"
    const val EXTRA_SHOW_ID = "EXTRA_SHOW_ID"
    const val EXTRA_MOVIE_ID = "EXTRA_MOVIE_ID"
    const val EXTRA_SETTINGS = "EXTRA_SETTINGS"
  }

  protected var settings: WidgetSettings? = null

  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
      settings = intent.getParcelableExtra(EXTRA_SETTINGS) as? WidgetSettings
    }
    super.onReceive(context, intent)
  }
}
