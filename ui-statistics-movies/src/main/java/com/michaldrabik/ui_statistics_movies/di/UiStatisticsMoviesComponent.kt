package com.michaldrabik.ui_statistics_movies.di

import com.michaldrabik.ui_statistics_movies.StatisticsMoviesFragment
import dagger.Subcomponent

@Subcomponent
interface UiStatisticsMoviesComponent {

  @Subcomponent.Factory
  interface Factory {
    fun create(): UiStatisticsMoviesComponent
  }

  fun inject(fragment: StatisticsMoviesFragment)
}
