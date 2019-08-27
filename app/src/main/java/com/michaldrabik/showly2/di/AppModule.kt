package com.michaldrabik.showly2.di

import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.ViewModelFactory
import dagger.Module
import dagger.Provides

@Module
object AppModule {

  @AppScope
  @JvmStatic
  @Provides
  fun providesViewModelFactory(cloud: Cloud): ViewModelFactory {
    return ViewModelFactory(cloud)
  }

}
