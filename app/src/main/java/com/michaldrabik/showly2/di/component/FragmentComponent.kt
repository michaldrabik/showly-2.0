package com.michaldrabik.showly2.di.component

import com.michaldrabik.showly2.ui.watchlist.WatchlistFragment
import com.michaldrabik.showly2.ui.watchlist.pages.upcoming.WatchlistUpcomingFragment
import com.michaldrabik.showly2.ui.watchlist.pages.watchlist.WatchlistMainFragment
import dagger.Subcomponent

@Subcomponent
interface FragmentComponent {

  @Subcomponent.Factory
  interface Factory {
    fun create(): FragmentComponent
  }

  fun inject(fragment: WatchlistFragment)

  fun inject(fragment: WatchlistUpcomingFragment)

  fun inject(fragment: WatchlistMainFragment)

}
