package com.michaldrabik.ui_statistics.di

import com.michaldrabik.ui_statistics.StatisticsFragment
import dagger.Subcomponent

@Subcomponent
interface UiStatisticsComponent {

  @Subcomponent.Factory
  interface Factory {
    fun create(): UiStatisticsComponent
  }

  fun inject(fragment: StatisticsFragment)
}
