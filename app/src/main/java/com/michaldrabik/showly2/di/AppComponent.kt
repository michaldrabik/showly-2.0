package com.michaldrabik.showly2.di

import com.michaldrabik.network.di.CloudComponent
import com.michaldrabik.showly2.ui.MainActivity
import com.michaldrabik.showly2.ui.discover.DiscoverFragment
import com.michaldrabik.showly2.ui.search.SearchFragment
import com.michaldrabik.showly2.ui.show.ShowDetailsFragment
import com.michaldrabik.showly2.ui.show.seasons.episodes.details.EpisodeDetailsBottomSheet
import com.michaldrabik.storage.di.StorageComponent
import dagger.Component

@AppScope
@Component(
  dependencies = [CloudComponent::class, StorageComponent::class],
  modules = [AppModule::class]
)
interface AppComponent {
  fun inject(activity: MainActivity)

  fun inject(fragment: DiscoverFragment)

  fun inject(fragment: ShowDetailsFragment)

  fun inject(fragment: EpisodeDetailsBottomSheet)

  fun inject(fragment: SearchFragment)
}

