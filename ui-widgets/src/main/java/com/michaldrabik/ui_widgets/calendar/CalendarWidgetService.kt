package com.michaldrabik.ui_widgets.calendar

import android.content.Intent
import android.widget.RemoteViewsService
import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.ui_progress.calendar.cases.items.CalendarFutureCase
import com.michaldrabik.ui_progress.calendar.cases.items.CalendarRecentsCase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CalendarWidgetService : RemoteViewsService() {

  @Inject lateinit var calendarFutureCase: CalendarFutureCase
  @Inject lateinit var calendarRecentsCase: CalendarRecentsCase
  @Inject lateinit var settingsRepository: SettingsRepository

  override fun onGetViewFactory(intent: Intent?) =
    CalendarWidgetViewsFactory(
      applicationContext,
      calendarFutureCase,
      calendarRecentsCase,
      settingsRepository
    )
}
