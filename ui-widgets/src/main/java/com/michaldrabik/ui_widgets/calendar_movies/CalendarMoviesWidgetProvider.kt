package com.michaldrabik.ui_widgets.calendar_movies

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_MUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE
import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.RemoteViews
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import com.michaldrabik.common.Config
import com.michaldrabik.common.Mode
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_model.CalendarMode
import com.michaldrabik.ui_widgets.BaseWidgetProvider
import com.michaldrabik.ui_widgets.R
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class CalendarMoviesWidgetProvider : BaseWidgetProvider() {

  companion object {
    fun requestUpdate(context: Context) {
      val applicationContext = context.applicationContext
      val intent = Intent(applicationContext, CalendarMoviesWidgetProvider::class.java).apply {
        val ids: IntArray = AppWidgetManager.getInstance(applicationContext)
          .getAppWidgetIds(ComponentName(applicationContext, CalendarMoviesWidgetProvider::class.java))
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
      isLight -> R.layout.widget_movies_calendar_day
      else -> R.layout.widget_movies_calendar_night
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
    val spaceTiny = context.dimenToPx(R.dimen.spaceTiny)

    val intent = Intent(context, CalendarMoviesWidgetService::class.java).apply {
      putExtra(EXTRA_APPWIDGET_ID, widgetId)
      data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
    }

    val remoteViews = RemoteViews(context.packageName, getLayoutResId()).apply {
      setRemoteAdapter(R.id.calendarWidgetMoviesList, intent)
      setEmptyView(R.id.calendarWidgetMoviesList, R.id.calendarWidgetMoviesEmptyView)

      val paddingTop = if (settings.widgetsShowLabel) context.dimenToPx(R.dimen.widgetPaddingTop) else spaceTiny
      val labelVisibility = if (settings.widgetsShowLabel) VISIBLE else GONE
      setViewPadding(R.id.calendarWidgetMoviesList, 0, paddingTop, 0, spaceTiny)
      setViewPadding(R.id.calendarWidgetMoviesEmptyView, 0, paddingTop, 0, 0)
      setViewVisibility(R.id.calendarWidgetMoviesLabel, labelVisibility)

      setInt(R.id.calendarWidgetMoviesNightRoot, "setBackgroundResource", getBackgroundResId())
      setInt(R.id.calendarWidgetMoviesDayRoot, "setBackgroundResource", getBackgroundResId())

      when (settingsRepository.widgets.getWidgetCalendarMode(Mode.MOVIES, widgetId)) {
        CalendarMode.PRESENT_FUTURE -> {
          setImageViewResource(R.id.calendarWidgetMoviesEmptyViewIcon, R.drawable.ic_history)
          setTextViewText(R.id.calendarWidgetMoviesEmptyViewSubtitle, context.getString(R.string.textMoviesCalendarEmpty))
        }
        CalendarMode.RECENTS -> {
          setImageViewResource(R.id.calendarWidgetMoviesEmptyViewIcon, R.drawable.ic_calendar)
          setTextViewText(R.id.calendarWidgetMoviesEmptyViewSubtitle, context.getString(R.string.textMoviesCalendarRecentsEmpty))
        }
      }
    }

    val listClickIntent = Intent(context, CalendarMoviesWidgetProvider::class.java).apply {
      action = ACTION_CLICK
      data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))
    }

    val mainIntent = PendingIntent.getActivity(
      context,
      0,
      Intent().apply { setClassName(context, Config.HOST_ACTIVITY_NAME) },
      FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
    )
    remoteViews.setOnClickPendingIntent(R.id.calendarWidgetMoviesLabelImage, mainIntent)
    remoteViews.setOnClickPendingIntent(R.id.calendarWidgetMoviesLabelText, mainIntent)

    val modeClickIntent = PendingIntent.getBroadcast(
      context,
      2,
      Intent(ACTION_CLICK).apply {
        setClass(context, this@CalendarMoviesWidgetProvider.javaClass)
        putExtra(EXTRA_MODE_CLICK, true)
        putExtra(EXTRA_APPWIDGET_ID, widgetId)
      },
      FLAG_MUTABLE or FLAG_UPDATE_CURRENT
    )
    remoteViews.setOnClickPendingIntent(R.id.calendarWidgetMoviesEmptyViewIcon, modeClickIntent)

    val listIntent = PendingIntent.getBroadcast(context, 0, listClickIntent, FLAG_MUTABLE or FLAG_UPDATE_CURRENT)
    remoteViews.setPendingIntentTemplate(R.id.calendarWidgetMoviesList, listIntent)

    appWidgetManager.updateAppWidget(widgetId, remoteViews)
    appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.calendarWidgetMoviesList)
  }

  private fun toggleCalendarMode(widgetId: Int) {
    when (settingsRepository.widgets.getWidgetCalendarMode(Mode.MOVIES, widgetId)) {
      CalendarMode.PRESENT_FUTURE ->
        settingsRepository.widgets.setWidgetCalendarMode(Mode.MOVIES, widgetId, CalendarMode.RECENTS)
      CalendarMode.RECENTS ->
        settingsRepository.widgets.setWidgetCalendarMode(Mode.MOVIES, widgetId, CalendarMode.PRESENT_FUTURE)
    }
  }

  override fun onReceive(context: Context, intent: Intent) {

    fun onListItemClick() {
      val movieId = intent.getLongExtra(EXTRA_MOVIE_ID, -1L)
      context.startActivity(
        Intent().apply {
          setClassName(context, Config.HOST_ACTIVITY_NAME)
          putExtra(EXTRA_MOVIE_ID, movieId.toString())
          flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
      )
    }

    fun onHeaderIconClick(widgetId: Int) {
      toggleCalendarMode(widgetId)
      requestUpdate(context.applicationContext)
    }

    super.onReceive(context, intent)
    if (intent.action == ACTION_CLICK) {
      when {
        intent.extras?.containsKey(EXTRA_MOVIE_ID) == true -> onListItemClick()
        intent.extras?.containsKey(EXTRA_MODE_CLICK) == true -> {
          val widgetId = intent.extras?.getInt(EXTRA_APPWIDGET_ID) ?: 0
          onHeaderIconClick(widgetId)
        }
      }
    }
  }
}
