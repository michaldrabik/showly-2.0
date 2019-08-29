package com.michaldrabik.showly2.di

import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.ViewModelFactory
import com.michaldrabik.storage.cache.ImagesUrlCache
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
    imagesCache: ImagesUrlCache
  ): ViewModelFactory =
    ViewModelFactory(cloud, userRepository, imagesCache)
}
