package com.michaldrabik.ui_widgets.calendar_movies

import android.content.Intent
import android.widget.RemoteViewsService
import com.michaldrabik.ui_base.images.MovieImagesProvider
import com.michaldrabik.ui_progress_movies.calendar.cases.ProgressMoviesCalendarCase
import com.michaldrabik.ui_progress_movies.main.cases.ProgressMoviesLoadItemsCase
import com.michaldrabik.ui_widgets.di.UiWidgetsComponentProvider
import javax.inject.Inject

class CalendarMoviesWidgetService : RemoteViewsService() {

  @Inject lateinit var progressLoadItemsCase: ProgressMoviesLoadItemsCase
  @Inject lateinit var calendarCase: ProgressMoviesCalendarCase
  @Inject lateinit var imagesProvider: MovieImagesProvider

  override fun onCreate() {
    super.onCreate()
    (application as UiWidgetsComponentProvider).provideWidgetsComponent().inject(this)
  }

  override fun onGetViewFactory(intent: Intent?) =
    CalendarMoviesWidgetViewsFactory(
      applicationContext,
      progressLoadItemsCase,
      calendarCase,
      imagesProvider
    )
}
