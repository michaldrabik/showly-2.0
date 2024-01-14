package com.michaldrabik.ui_widgets.progress_movies

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_MUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.RemoteViews
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import com.michaldrabik.common.Config
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_widgets.BaseWidgetProvider
import com.michaldrabik.ui_widgets.R
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class ProgressMoviesWidgetProvider : BaseWidgetProvider() {

  companion object {
    const val EXTRA_CHECK_MOVIE_ID = "EXTRA_CHECK_MOVIE_ID"

    fun requestUpdate(context: Context) {
      val applicationContext = context.applicationContext
      val intent = Intent(applicationContext, ProgressMoviesWidgetProvider::class.java).apply {
        val ids: IntArray = AppWidgetManager.getInstance(applicationContext)
          .getAppWidgetIds(ComponentName(applicationContext, ProgressMoviesWidgetProvider::class.java))
        action = ACTION_APPWIDGET_UPDATE
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
      }
      applicationContext.sendBroadcast(intent)
      Timber.d("Widget update requested.")
    }
  }

  override fun getLayoutResId(): Int {
    val isLight = settingsRepository.widgets.widgetsTheme == MODE_NIGHT_NO
    return when {
      isLight -> R.layout.widget_movies_progress_day
      else -> R.layout.widget_movies_progress_night
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
    val intent = Intent(context, ProgressMoviesWidgetService::class.java).apply {
      putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
      data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
    }

    val remoteViews = RemoteViews(context.packageName, getLayoutResId()).apply {
      setRemoteAdapter(R.id.progressWidgetMoviesList, intent)
      setEmptyView(R.id.progressWidgetMoviesList, R.id.progressWidgetMoviesEmptyView)

      val spaceTiny = context.dimenToPx(R.dimen.spaceTiny)
      val paddingTop = if (settings.widgetsShowLabel) context.dimenToPx(R.dimen.widgetPaddingTop) else spaceTiny
      val labelVisibility = if (settings.widgetsShowLabel) VISIBLE else GONE
      setViewPadding(R.id.progressWidgetMoviesList, 0, paddingTop, 0, spaceTiny)
      setViewVisibility(R.id.progressWidgetMoviesLabel, labelVisibility)

      setInt(R.id.progressWidgetMoviesNightRoot, "setBackgroundResource", getBackgroundResId())
      setInt(R.id.progressWidgetMoviesDayRoot, "setBackgroundResource", getBackgroundResId())
    }

    val mainIntent = PendingIntent.getActivity(
      context,
      0,
      Intent().apply { setClassName(context, Config.HOST_ACTIVITY_NAME) },
      FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
    )
    remoteViews.setOnClickPendingIntent(R.id.progressWidgetMoviesLabel, mainIntent)

    val listClickIntent = Intent(context, ProgressMoviesWidgetProvider::class.java).apply {
      action = ACTION_CLICK
      data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))
    }

    val listIntent = PendingIntent.getBroadcast(context, 0, listClickIntent, FLAG_MUTABLE or FLAG_UPDATE_CURRENT)
    remoteViews.setPendingIntentTemplate(R.id.progressWidgetMoviesList, listIntent)

    appWidgetManager.updateAppWidget(widgetId, remoteViews)
    appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.progressWidgetMoviesList)
  }

  override fun onReceive(context: Context, intent: Intent) {
    super.onReceive(context, intent)
    if (intent.action == ACTION_CLICK) {
      when {
        intent.extras?.containsKey(EXTRA_MOVIE_ID) == true -> {
          val movieId = intent.getLongExtra(EXTRA_MOVIE_ID, -1L)
          context.startActivity(
            Intent().apply {
              setClassName(context, Config.HOST_ACTIVITY_NAME)
              putExtra(EXTRA_MOVIE_ID, movieId.toString())
              flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
          )
        }
        intent.extras?.containsKey(EXTRA_CHECK_MOVIE_ID) == true -> {
          val movieId = intent.getLongExtra(EXTRA_CHECK_MOVIE_ID, -1L)
          ProgressMoviesWidgetCheckService.initialize(
            context.applicationContext,
            IdTrakt(movieId)
          )
        }
      }
    }
  }
}
