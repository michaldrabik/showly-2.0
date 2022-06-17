package com.michaldrabik.ui_widgets.progress

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_MUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE
import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID
import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS
import android.appwidget.AppWidgetManager.getInstance
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.URI_INTENT_SCHEME
import android.net.Uri
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.RemoteViews
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import com.michaldrabik.common.Config.HOST_ACTIVITY_NAME
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_widgets.BaseWidgetProvider
import com.michaldrabik.ui_widgets.R
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class ProgressWidgetProvider : BaseWidgetProvider() {

  companion object {
    const val EXTRA_SEASON_ID = "EXTRA_SEASON_ID"
    const val EXTRA_EPISODE_ID = "EXTRA_EPISODE_ID"

    fun requestUpdate(context: Context) {
      val applicationContext = context.applicationContext
      val intent = Intent(applicationContext, ProgressWidgetProvider::class.java).apply {
        val ids: IntArray = getInstance(applicationContext)
          .getAppWidgetIds(ComponentName(applicationContext, ProgressWidgetProvider::class.java))
        action = ACTION_APPWIDGET_UPDATE
        putExtra(EXTRA_APPWIDGET_IDS, ids)
      }
      applicationContext.sendBroadcast(intent)
      Timber.d("Widget update requested.")
    }
  }

  override fun getLayoutResId(): Int {
    val isLight = settingsRepository.widgets.widgetsTheme == MODE_NIGHT_NO
    return when {
      isLight -> R.layout.widget_progress_day
      else -> R.layout.widget_progress_night
    }
  }

  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray?,
  ) {
    super.onUpdate(context, appWidgetManager, appWidgetIds)
    appWidgetIds?.forEach { updateWidget(context, appWidgetManager, it) }
  }

  private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
    val intent = Intent(context, ProgressWidgetService::class.java).apply {
      putExtra(EXTRA_APPWIDGET_ID, widgetId)
      data = Uri.parse(toUri(URI_INTENT_SCHEME))
    }

    val remoteViews = RemoteViews(context.packageName, getLayoutResId()).apply {
      setRemoteAdapter(R.id.progressWidgetList, intent)
      setEmptyView(R.id.progressWidgetList, R.id.progressWidgetEmptyView)

      val spaceTiny = context.dimenToPx(R.dimen.spaceTiny)
      val paddingTop = if (settings.widgetsShowLabel) context.dimenToPx(R.dimen.widgetPaddingTop) else spaceTiny
      val labelVisibility = if (settings.widgetsShowLabel) VISIBLE else GONE
      setViewPadding(R.id.progressWidgetList, 0, paddingTop, 0, spaceTiny)
      setViewVisibility(R.id.progressWidgetLabel, labelVisibility)

      setInt(R.id.progressWidgetNightRoot, "setBackgroundResource", getBackgroundResId())
      setInt(R.id.progressWidgetDayRoot, "setBackgroundResource", getBackgroundResId())
    }

    val mainIntent = PendingIntent.getActivity(
      context,
      0,
      Intent().apply { setClassName(context, HOST_ACTIVITY_NAME) },
      FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
    )
    remoteViews.setOnClickPendingIntent(R.id.progressWidgetLabel, mainIntent)

    val listClickIntent = Intent(context, ProgressWidgetProvider::class.java).apply {
      action = ACTION_CLICK
      data = Uri.parse(intent.toUri(URI_INTENT_SCHEME))
    }
    val showDetailsPendingIntent = PendingIntent.getBroadcast(context, 0, listClickIntent, FLAG_MUTABLE or FLAG_UPDATE_CURRENT)
    remoteViews.setPendingIntentTemplate(R.id.progressWidgetList, showDetailsPendingIntent)

    appWidgetManager.updateAppWidget(widgetId, remoteViews)
    appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.progressWidgetList)
  }

  override fun onReceive(context: Context, intent: Intent) {
    super.onReceive(context, intent)
    if (intent.action.equals(ACTION_CLICK)) {
      when {
        intent.extras?.containsKey(EXTRA_EPISODE_ID) == true -> {
          val episodeId = intent.getLongExtra(EXTRA_EPISODE_ID, -1L)
          val seasonId = intent.getLongExtra(EXTRA_SEASON_ID, -1L)
          val showId = intent.getLongExtra(EXTRA_SHOW_ID, -1L)
          ProgressWidgetEpisodeCheckService.initialize(
            context.applicationContext,
            episodeId,
            seasonId,
            IdTrakt(showId)
          )
        }
        intent.extras?.containsKey(EXTRA_SHOW_ID) == true -> {
          val showId = intent.getLongExtra(EXTRA_SHOW_ID, -1L)
          context.startActivity(
            Intent().apply {
              setClassName(context, HOST_ACTIVITY_NAME)
              putExtra(EXTRA_SHOW_ID, showId.toString())
              flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
          )
        }
      }
    }
  }
}
