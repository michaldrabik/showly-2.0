package com.michaldrabik.data_remote.di.module

import com.michaldrabik.data_remote.di.CloudScope
import com.michaldrabik.data_remote.tmdb.TmdbInterceptor
import com.michaldrabik.data_remote.tmdb.api.TmdbApi
import com.michaldrabik.data_remote.tmdb.api.TmdbService
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Named

@Module
object TmdbModule {

  @Provides
  @CloudScope
  fun providesTmdbApi(@Named("retrofitTmdb") retrofit: Retrofit): TmdbApi =
    TmdbApi(retrofit.create(TmdbService::class.java))

  @Provides
  @CloudScope
  fun providesTmdbInterceptor() = TmdbInterceptor()
}
