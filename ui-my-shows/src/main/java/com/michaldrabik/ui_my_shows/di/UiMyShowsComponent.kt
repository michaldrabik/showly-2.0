package com.michaldrabik.ui_my_shows.di

import com.michaldrabik.ui_my_shows.archive.ArchiveFragment
import com.michaldrabik.ui_my_shows.main.FollowedShowsFragment
import com.michaldrabik.ui_my_shows.myshows.MyShowsFragment
import com.michaldrabik.ui_my_shows.watchlist.WatchlistFragment
import dagger.Subcomponent

@Subcomponent
interface UiMyShowsComponent {

  @Subcomponent.Factory
  interface Factory {
    fun create(): UiMyShowsComponent
  }

  fun inject(fragment: FollowedShowsFragment)

  fun inject(fragment: MyShowsFragment)

  fun inject(fragment: WatchlistFragment)

  fun inject(fragment: ArchiveFragment)
}
