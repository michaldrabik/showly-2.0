package com.michaldrabik.showly2.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE
import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID
import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS
import android.appwidget.AppWidgetManager.getInstance
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.URI_INTENT_SCHEME
import android.net.Uri
import android.widget.RemoteViews
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.ui.main.MainActivity

class WatchlistAppWidgetProvider : AppWidgetProvider() {

  companion object {
    const val ACTION_SHOW_DETAILS = "ACTION_SHOW_DETAILS"
    const val EXTRA_SHOW_ID = "EXTRA_SHOW_ID"

    fun requestUpdate(context: Context) {
      val applicationContext = context.applicationContext
      val intent = Intent(applicationContext, WatchlistAppWidgetProvider::class.java).apply {
        val ids: IntArray = getInstance(applicationContext)
          .getAppWidgetIds(ComponentName(applicationContext, WatchlistAppWidgetProvider::class.java))
        action = ACTION_APPWIDGET_UPDATE
        putExtra(EXTRA_APPWIDGET_IDS, ids)
      }
      applicationContext.sendBroadcast(intent)
    }
  }

  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray?
  ) {
    appWidgetIds?.forEach { updateWidget(context, appWidgetManager, it) }
    super.onUpdate(context, appWidgetManager, appWidgetIds)
  }

  private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
    val intent = Intent(context, WatchlistWidgetService::class.java).apply {
      putExtra(EXTRA_APPWIDGET_ID, widgetId)
      data = Uri.parse(toUri(URI_INTENT_SCHEME))
    }

    val remoteViews = RemoteViews(context.packageName, R.layout.widget_watchlist).apply {
      setRemoteAdapter(R.id.watchlistWidgetList, intent)
      setEmptyView(R.id.watchlistWidgetList, R.id.watchlistWidgetEmptyView)
    }

    val showDetailsIntent = Intent(context, WatchlistAppWidgetProvider::class.java).apply {
      action = ACTION_SHOW_DETAILS
      data = Uri.parse(intent.toUri(URI_INTENT_SCHEME));
    }
    val pendingIntent = PendingIntent.getBroadcast(context, 0, showDetailsIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    remoteViews.setPendingIntentTemplate(R.id.watchlistWidgetList, pendingIntent)

    appWidgetManager.updateAppWidget(widgetId, remoteViews)
    appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.watchlistWidgetList)
  }

  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action.equals(ACTION_SHOW_DETAILS)) {
      val showId = intent.getLongExtra(EXTRA_SHOW_ID, -1L)
      context.startActivity(Intent(context, MainActivity::class.java).apply {
        putExtra(EXTRA_SHOW_ID, showId)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
      })
    }
    super.onReceive(context, intent)
  }
}
