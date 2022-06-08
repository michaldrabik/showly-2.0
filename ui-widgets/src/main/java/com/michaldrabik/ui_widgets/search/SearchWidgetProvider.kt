package com.michaldrabik.ui_widgets.search

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.michaldrabik.common.Config.HOST_ACTIVITY_NAME
import com.michaldrabik.ui_widgets.R

class SearchWidgetProvider : AppWidgetProvider() {

  companion object {
    const val EXTRA_WIDGET_SEARCH_CLICK = "EXTRA_WIDGET_SEARCH_CLICK"
  }

  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray?
  ) {
    appWidgetIds?.forEach { updateWidget(context, appWidgetManager, it) }
    super.onUpdate(context, appWidgetManager, appWidgetIds)
  }

  private fun updateWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    widgetId: Int
  ) {
    val remoteViews = RemoteViews(context.packageName, R.layout.widget_search).apply {
      val intent = Intent().apply {
        setClassName(context, HOST_ACTIVITY_NAME)
        putExtra(EXTRA_WIDGET_SEARCH_CLICK, true)
      }
      val pendingIntent = PendingIntent.getActivity(context, 2, intent, FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT)
      setOnClickPendingIntent(R.id.searchWidgetRoot, pendingIntent)
    }
    appWidgetManager.updateAppWidget(widgetId, remoteViews)
  }
}
