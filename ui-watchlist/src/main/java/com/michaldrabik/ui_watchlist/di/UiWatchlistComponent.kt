package com.michaldrabik.ui_watchlist.di

import com.michaldrabik.ui_watchlist.main.WatchlistFragment
import com.michaldrabik.ui_watchlist.upcoming.WatchlistUpcomingFragment
import com.michaldrabik.ui_watchlist.watchlist.WatchlistMainFragment
import dagger.Subcomponent

@Subcomponent
interface UiWatchlistComponent {

  @Subcomponent.Factory
  interface Factory {
    fun create(): UiWatchlistComponent
  }

  fun inject(fragment: WatchlistFragment)

  fun inject(fragment: WatchlistMainFragment)

  fun inject(fragment: WatchlistUpcomingFragment)
}
