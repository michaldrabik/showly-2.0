package com.michaldrabik.network2.di

import com.michaldrabik.network2.api.TraktApi
import dagger.Module
import dagger.Provides

@Module
object CloudModule {

  @Provides
  @JvmStatic
  fun providesTraktApi() = TraktApi()
}