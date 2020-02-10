package com.michaldrabik.showly2.di

import com.michaldrabik.network.di.CloudMarker
import com.michaldrabik.showly2.common.ShowsSyncService
import com.michaldrabik.showly2.common.trakt.exports.TraktExportService
import com.michaldrabik.showly2.common.trakt.imports.TraktImportService
import com.michaldrabik.showly2.di.module.PreferencesModule
import com.michaldrabik.showly2.di.module.ViewModelsModule
import com.michaldrabik.showly2.ui.discover.DiscoverFragment
import com.michaldrabik.showly2.ui.followedshows.FollowedShowsFragment
import com.michaldrabik.showly2.ui.followedshows.myshows.MyShowsFragment
import com.michaldrabik.showly2.ui.followedshows.seelater.SeeLaterFragment
import com.michaldrabik.showly2.ui.main.MainActivity
import com.michaldrabik.showly2.ui.search.SearchFragment
import com.michaldrabik.showly2.ui.settings.SettingsFragment
import com.michaldrabik.showly2.ui.show.ShowDetailsFragment
import com.michaldrabik.showly2.ui.show.gallery.FanartGalleryFragment
import com.michaldrabik.showly2.ui.show.seasons.episodes.details.EpisodeDetailsBottomSheet
import com.michaldrabik.showly2.ui.trakt.exports.TraktExportFragment
import com.michaldrabik.showly2.ui.trakt.imports.TraktImportFragment
import com.michaldrabik.showly2.ui.watchlist.WatchlistFragment
import com.michaldrabik.storage.di.StorageMarker
import dagger.Component

@AppScope
@Component(
  dependencies = [CloudMarker::class, StorageMarker::class],
  modules = [ViewModelsModule::class, PreferencesModule::class]
)
interface AppComponent {
  fun inject(activity: MainActivity)

  fun inject(fragment: DiscoverFragment)

  fun inject(fragment: ShowDetailsFragment)

  fun inject(fragment: FanartGalleryFragment)

  fun inject(fragment: EpisodeDetailsBottomSheet)

  fun inject(fragment: SearchFragment)

  fun inject(fragment: FollowedShowsFragment)

  fun inject(fragment: MyShowsFragment)

  fun inject(fragment: SeeLaterFragment)

  fun inject(fragment: WatchlistFragment)

  fun inject(fragment: SettingsFragment)

  fun inject(fragment: TraktImportFragment)

  fun inject(fragment: TraktExportFragment)

  fun inject(service: ShowsSyncService)

  fun inject(service: TraktImportService)

  fun inject(service: TraktExportService)
}
