package com.michaldrabik.ui_widgets.calendar_movies

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.widget.RemoteViewsService
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_progress_movies.calendar.cases.items.CalendarMoviesFutureCase
import com.michaldrabik.ui_progress_movies.calendar.cases.items.CalendarMoviesRecentsCase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CalendarMoviesWidgetService : RemoteViewsService() {

  @Inject lateinit var calendarFutureCase: CalendarMoviesFutureCase
  @Inject lateinit var calendarRecentsCase: CalendarMoviesRecentsCase
  @Inject lateinit var settingsRepository: SettingsRepository

  override fun onGetViewFactory(intent: Intent?): CalendarMoviesWidgetViewsFactory {
    val widgetId = intent?.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID) ?: 0
    return CalendarMoviesWidgetViewsFactory(
      widgetId,
      applicationContext,
      calendarFutureCase,
      calendarRecentsCase,
      settingsRepository
    )
  }
}
