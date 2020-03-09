package com.michaldrabik.showly2.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.Intent.URI_INTENT_SCHEME
import android.net.Uri
import android.widget.RemoteViews
import com.michaldrabik.showly2.R

class WatchlistAppWidgetProvider : AppWidgetProvider() {

  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray?
  ) {
    appWidgetIds?.forEach { updateWidget(context, appWidgetManager, it) }
  }

  private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
    val intent = Intent(context, WatchlistWidgetService::class.java).apply {
      putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
      data = Uri.parse(toUri(URI_INTENT_SCHEME))
    }

    val remoteViews = RemoteViews(context.packageName, R.layout.widget_watchlist).apply {
      setRemoteAdapter(R.id.watchlistWidgetList, intent)
      setEmptyView(R.id.watchlistWidgetList, R.id.watchlistWidgetEmptyView)
    }

    appWidgetManager.updateAppWidget(widgetId, remoteViews)
  }
}
