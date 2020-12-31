package com.michaldrabik.ui_widgets.di

import com.michaldrabik.ui_widgets.calendar.CalendarWidgetProvider
import com.michaldrabik.ui_widgets.calendar.CalendarWidgetService
import com.michaldrabik.ui_widgets.calendar_movies.CalendarMoviesWidgetProvider
import com.michaldrabik.ui_widgets.calendar_movies.CalendarMoviesWidgetService
import com.michaldrabik.ui_widgets.progress.ProgressWidgetEpisodeCheckService
import com.michaldrabik.ui_widgets.progress.ProgressWidgetProvider
import com.michaldrabik.ui_widgets.progress.ProgressWidgetService
import com.michaldrabik.ui_widgets.progress_movies.ProgressMoviesWidgetCheckService
import com.michaldrabik.ui_widgets.progress_movies.ProgressMoviesWidgetProvider
import com.michaldrabik.ui_widgets.progress_movies.ProgressMoviesWidgetService
import dagger.Subcomponent

@Subcomponent
interface UiWidgetsComponent {

  @Subcomponent.Factory
  interface Factory {
    fun create(): UiWidgetsComponent
  }

  fun inject(service: ProgressWidgetEpisodeCheckService)
  fun inject(service: ProgressWidgetService)

  fun inject(service: ProgressMoviesWidgetCheckService)
  fun inject(service: ProgressMoviesWidgetService)

  fun inject(service: CalendarWidgetService)
  fun inject(service: CalendarMoviesWidgetService)

  fun inject(provider: ProgressWidgetProvider)
  fun inject(provider: ProgressMoviesWidgetProvider)

  fun inject(provider: CalendarWidgetProvider)
  fun inject(provider: CalendarMoviesWidgetProvider)
}
