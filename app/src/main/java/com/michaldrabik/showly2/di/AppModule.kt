package com.michaldrabik.showly2.di

import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.ui.ViewModelFactory
import com.michaldrabik.showly2.ui.discover.DiscoverInteractor
import com.michaldrabik.storage.repository.UserRepository
import dagger.Module
import dagger.Provides

@Module
object AppModule {

  @AppScope
  @JvmStatic
  @Provides
  fun providesViewModelFactory(
    cloud: Cloud,
    userRepository: UserRepository,
    discoverInteractor: DiscoverInteractor
  ): ViewModelFactory =
    ViewModelFactory(cloud, userRepository, discoverInteractor)
}
