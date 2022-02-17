package com.michaldrabik.ui_widgets.calendar_movies

import android.content.Intent
import android.widget.RemoteViewsService
import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.ui_progress_movies.calendar.cases.items.CalendarMoviesFutureCase
import com.michaldrabik.ui_progress_movies.calendar.cases.items.CalendarMoviesRecentsCase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CalendarMoviesWidgetService : RemoteViewsService() {

  @Inject lateinit var calendarFutureCase: CalendarMoviesFutureCase
  @Inject lateinit var calendarRecentsCase: CalendarMoviesRecentsCase
  @Inject lateinit var settingsRepository: SettingsRepository

  override fun onGetViewFactory(intent: Intent?) =
    CalendarMoviesWidgetViewsFactory(
      applicationContext,
      calendarFutureCase,
      calendarRecentsCase,
      settingsRepository
    )
}
