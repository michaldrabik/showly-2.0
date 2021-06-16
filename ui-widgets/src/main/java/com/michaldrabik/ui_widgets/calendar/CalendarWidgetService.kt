package com.michaldrabik.ui_widgets.calendar

import android.content.Intent
import android.widget.RemoteViewsService
import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_progress.calendar.cases.items.CalendarFutureCase
import com.michaldrabik.ui_progress.main.cases.ProgressLoadItemsCase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CalendarWidgetService : RemoteViewsService() {

  @Inject lateinit var progressLoadItemsCase: ProgressLoadItemsCase
  @Inject lateinit var calendarCase: CalendarFutureCase
  @Inject lateinit var imagesProvider: ShowImagesProvider
  @Inject lateinit var settingsRepository: SettingsRepository

  override fun onGetViewFactory(intent: Intent?) =
    CalendarWidgetViewsFactory(
      applicationContext,
      progressLoadItemsCase,
      calendarCase,
      imagesProvider,
      settingsRepository
    )
}
