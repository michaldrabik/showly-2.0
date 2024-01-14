package com.michaldrabik.data_remote.di.module

import com.michaldrabik.data_remote.omdb.OmdbInterceptor
import com.michaldrabik.data_remote.omdb.OmdbRemoteDataSource
import com.michaldrabik.data_remote.omdb.api.OmdbApi
import com.michaldrabik.data_remote.omdb.api.OmdbService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OmdbModule {

  @Provides
  @Singleton
  fun providesOmdbApi(@Named("retrofitOmdb") retrofit: Retrofit): OmdbRemoteDataSource =
    OmdbApi(retrofit.create(OmdbService::class.java))

  @Provides
  @Singleton
  fun providesOmdbInterceptor() = OmdbInterceptor()
}
