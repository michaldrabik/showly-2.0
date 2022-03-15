package com.michaldrabik.data_remote.di.module

import com.michaldrabik.data_remote.tmdb.TmdbInterceptor
import com.michaldrabik.data_remote.tmdb.TmdbRemoteDataSource
import com.michaldrabik.data_remote.tmdb.api.TmdbApi
import com.michaldrabik.data_remote.tmdb.api.TmdbService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TmdbModule {

  @Provides
  @Singleton
  fun providesTmdbApi(@Named("retrofitTmdb") retrofit: Retrofit): TmdbRemoteDataSource =
    TmdbApi(retrofit.create(TmdbService::class.java))

  @Provides
  @Singleton
  fun providesTmdbInterceptor() = TmdbInterceptor()
}
