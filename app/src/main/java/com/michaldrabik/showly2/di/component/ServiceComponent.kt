package com.michaldrabik.showly2.di.component

import com.michaldrabik.showly2.common.ShowsSyncService
import com.michaldrabik.showly2.common.trakt.TraktSyncService
import com.michaldrabik.showly2.common.trakt.quicksync.QuickSyncService
import com.michaldrabik.showly2.widget.watchlist.WatchlistWidgetEpisodeCheckService
import com.michaldrabik.showly2.widget.watchlist.WatchlistWidgetService
import dagger.Subcomponent

@Subcomponent
interface ServiceComponent {

  @Subcomponent.Factory
  interface Factory {
    fun create(): ServiceComponent
  }

  fun inject(service: ShowsSyncService)

  fun inject(service: TraktSyncService)

  fun inject(service: WatchlistWidgetService)

  fun inject(service: WatchlistWidgetEpisodeCheckService)

  fun inject(service: QuickSyncService)
}
