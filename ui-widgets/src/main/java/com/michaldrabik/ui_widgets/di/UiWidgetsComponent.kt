package com.michaldrabik.ui_widgets.di

import com.michaldrabik.ui_widgets.progress.ProgressWidgetEpisodeCheckService
import com.michaldrabik.ui_widgets.progress.ProgressWidgetService
import dagger.Subcomponent

@Subcomponent
interface UiWidgetsComponent {

  @Subcomponent.Factory
  interface Factory {
    fun create(): UiWidgetsComponent
  }

  fun inject(service: ProgressWidgetEpisodeCheckService)
  fun inject(service: ProgressWidgetService)
}
