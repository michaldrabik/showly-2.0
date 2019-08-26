package com.michaldrabik.showly2.di

import com.michaldrabik.network.di.CloudComponent
import com.michaldrabik.showly2.MainActivity
import com.michaldrabik.showly2.discover.DiscoverFragment
import dagger.Component

@AppScope
@Component(
  dependencies = [CloudComponent::class],
  modules = [AppModule::class]
)
interface AppComponent {
  fun inject(activity: MainActivity)

  fun inject(fragment: DiscoverFragment)
}

