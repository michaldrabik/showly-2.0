package com.michaldrabik.data_remote.di.module

import com.michaldrabik.data_remote.trakt.TraktInterceptor
import com.michaldrabik.data_remote.trakt.api.TraktApi
import com.michaldrabik.data_remote.trakt.api.TraktService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TraktModule {

  @Provides
  @Singleton
  fun providesTraktApi(@Named("retrofitTrakt") retrofit: Retrofit): TraktApi =
    TraktApi(retrofit.create(TraktService::class.java))

  @Provides
  @Singleton
  fun providesTraktInterceptor() = TraktInterceptor()
}
