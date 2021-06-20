package com.michaldrabik.ui_widgets.calendar_movies

import android.content.Intent
import android.widget.RemoteViewsService
import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.ui_progress_movies.calendar.cases.items.CalendarMoviesFutureCase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CalendarMoviesWidgetService : RemoteViewsService() {

  @Inject lateinit var calendarCase: CalendarMoviesFutureCase
  @Inject lateinit var settingsRepository: SettingsRepository

  override fun onGetViewFactory(intent: Intent?) =
    CalendarMoviesWidgetViewsFactory(
      applicationContext,
      calendarCase,
      settingsRepository
    )
}
