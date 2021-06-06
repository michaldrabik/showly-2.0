package com.michaldrabik.ui_widgets.calendar_movies

import android.content.Intent
import android.widget.RemoteViewsService
import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.ui_base.images.MovieImagesProvider
import com.michaldrabik.ui_progress_movies.calendar.cases.ProgressMoviesCalendarCase
import com.michaldrabik.ui_progress_movies.main.cases.ProgressMoviesLoadItemsCase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CalendarMoviesWidgetService : RemoteViewsService() {

  @Inject lateinit var progressLoadItemsCase: ProgressMoviesLoadItemsCase
  @Inject lateinit var calendarCase: ProgressMoviesCalendarCase
  @Inject lateinit var imagesProvider: MovieImagesProvider
  @Inject lateinit var settingsRepository: SettingsRepository

  override fun onGetViewFactory(intent: Intent?) =
    CalendarMoviesWidgetViewsFactory(
      applicationContext,
      progressLoadItemsCase,
      calendarCase,
      imagesProvider,
      settingsRepository
    )
}
