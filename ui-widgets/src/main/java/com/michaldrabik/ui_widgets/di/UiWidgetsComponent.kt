package com.michaldrabik.ui_widgets.di

import com.michaldrabik.ui_widgets.watchlist.WatchlistWidgetEpisodeCheckService
import com.michaldrabik.ui_widgets.watchlist.WatchlistWidgetService
import dagger.Subcomponent

@Subcomponent
interface UiWidgetsComponent {

  @Subcomponent.Factory
  interface Factory {
    fun create(): UiWidgetsComponent
  }

  fun inject(service: WatchlistWidgetEpisodeCheckService)
  fun inject(service: WatchlistWidgetService)
}
