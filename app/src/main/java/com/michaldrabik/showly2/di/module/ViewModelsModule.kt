package com.michaldrabik.showly2.di.module

import androidx.lifecycle.ViewModel
import com.michaldrabik.showly2.di.ViewModelKey
import com.michaldrabik.showly2.ui.discover.DiscoverViewModel
import com.michaldrabik.showly2.ui.followedshows.FollowedShowsViewModel
import com.michaldrabik.showly2.ui.followedshows.archive.ArchiveViewModel
import com.michaldrabik.showly2.ui.followedshows.myshows.MyShowsViewModel
import com.michaldrabik.showly2.ui.followedshows.seelater.SeeLaterViewModel
import com.michaldrabik.showly2.ui.main.MainViewModel
import com.michaldrabik.showly2.ui.search.SearchViewModel
import com.michaldrabik.showly2.ui.settings.SettingsViewModel
import com.michaldrabik.showly2.ui.show.ShowDetailsViewModel
import com.michaldrabik.showly2.ui.show.gallery.FanartGalleryViewModel
import com.michaldrabik.showly2.ui.show.seasons.episodes.details.EpisodeDetailsViewModel
import com.michaldrabik.showly2.ui.statistics.StatisticsViewModel
import com.michaldrabik.showly2.ui.trakt.TraktSyncViewModel
import com.michaldrabik.showly2.ui.watchlist.WatchlistViewModel
import com.michaldrabik.showly2.ui.watchlist.pages.upcoming.WatchlistUpcomingViewModel
import com.michaldrabik.showly2.ui.watchlist.pages.watchlist.WatchlistMainViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Suppress("unused")
@Module
abstract class ViewModelsModule {

  @Binds
  @IntoMap
  @ViewModelKey(MainViewModel::class)
  abstract fun bindMainViewModel(viewModel: MainViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(SettingsViewModel::class)
  abstract fun bindSettingsViewModel(viewModel: SettingsViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(DiscoverViewModel::class)
  abstract fun bindDiscoverViewModel(viewModel: DiscoverViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(EpisodeDetailsViewModel::class)
  abstract fun bindEpisodeDetailsViewModel(viewModel: EpisodeDetailsViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(FanartGalleryViewModel::class)
  abstract fun bindFanartGalleryViewModel(viewModel: FanartGalleryViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(MyShowsViewModel::class)
  abstract fun bindMyShowsViewModel(viewModel: MyShowsViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(SearchViewModel::class)
  abstract fun bindSearchViewModel(viewModel: SearchViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(SeeLaterViewModel::class)
  abstract fun bindSeeLaterViewModel(viewModel: SeeLaterViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(ArchiveViewModel::class)
  abstract fun bindArchiveViewModel(viewModel: ArchiveViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(ShowDetailsViewModel::class)
  abstract fun bindShowDetailsViewModel(viewModel: ShowDetailsViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(WatchlistViewModel::class)
  abstract fun bindWatchlistMainViewModel(viewModel: WatchlistViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(WatchlistMainViewModel::class)
  abstract fun bindWatchlistViewModel(viewModel: WatchlistMainViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(WatchlistUpcomingViewModel::class)
  abstract fun bindWatchlistUpcomingViewModel(viewModel: WatchlistUpcomingViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(FollowedShowsViewModel::class)
  abstract fun bindFollowedShowsViewModel(viewModel: FollowedShowsViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(TraktSyncViewModel::class)
  abstract fun bindTraktSyncViewModel(viewModel: TraktSyncViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(StatisticsViewModel::class)
  abstract fun bindStatisticsViewModel(viewModel: StatisticsViewModel): ViewModel
}
