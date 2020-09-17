package com.michaldrabik.showly2.di.component

import com.michaldrabik.showly2.ui.discover.DiscoverFragment
import com.michaldrabik.showly2.ui.followedshows.FollowedShowsFragment
import com.michaldrabik.showly2.ui.followedshows.archive.ArchiveFragment
import com.michaldrabik.showly2.ui.followedshows.myshows.MyShowsFragment
import com.michaldrabik.showly2.ui.followedshows.seelater.SeeLaterFragment
import com.michaldrabik.showly2.ui.search.SearchFragment
import com.michaldrabik.showly2.ui.settings.SettingsFragment
import com.michaldrabik.showly2.ui.show.ShowDetailsFragment
import com.michaldrabik.showly2.ui.show.gallery.FanartGalleryFragment
import com.michaldrabik.showly2.ui.show.seasons.episodes.details.EpisodeDetailsBottomSheet
import com.michaldrabik.showly2.ui.statistics.StatisticsFragment
import com.michaldrabik.showly2.ui.trakt.TraktSyncFragment
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

  fun inject(fragment: DiscoverFragment)

  fun inject(fragment: ShowDetailsFragment)

  fun inject(fragment: FanartGalleryFragment)

  fun inject(fragment: SearchFragment)

  fun inject(fragment: FollowedShowsFragment)

  fun inject(fragment: MyShowsFragment)

  fun inject(fragment: SeeLaterFragment)

  fun inject(fragment: ArchiveFragment)

  fun inject(fragment: SettingsFragment)

  fun inject(fragment: TraktSyncFragment)

  fun inject(fragment: StatisticsFragment)

  fun inject(fragment: EpisodeDetailsBottomSheet)
}
