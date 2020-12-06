package com.michaldrabik.ui_progress_movies.di

import com.michaldrabik.ui_progress_movies.calendar.ProgressMoviesCalendarFragment
import com.michaldrabik.ui_progress_movies.main.ProgressMoviesFragment
import com.michaldrabik.ui_progress_movies.progress.ProgressMoviesMainFragment
import dagger.Subcomponent

@Subcomponent
interface UiProgressMoviesComponent {

  @Subcomponent.Factory
  interface Factory {
    fun create(): UiProgressMoviesComponent
  }

  fun inject(fragment: ProgressMoviesFragment)

  fun inject(fragment: ProgressMoviesMainFragment)

  fun inject(fragment: ProgressMoviesCalendarFragment)
}
