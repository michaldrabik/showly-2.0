package com.michaldrabik.ui_progress.di

import com.michaldrabik.ui_progress.calendar.ProgressCalendarFragment
import com.michaldrabik.ui_progress.main.ProgressFragment
import com.michaldrabik.ui_progress.progress.ProgressMainFragment
import dagger.Subcomponent

@Subcomponent
interface UiProgressComponent {

  @Subcomponent.Factory
  interface Factory {
    fun create(): UiProgressComponent
  }

  fun inject(fragment: ProgressFragment)

  fun inject(fragment: ProgressMainFragment)

  fun inject(fragment: ProgressCalendarFragment)
}
