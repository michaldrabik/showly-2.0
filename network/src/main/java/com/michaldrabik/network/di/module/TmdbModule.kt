package com.michaldrabik.network.di.module

import com.michaldrabik.network.di.CloudScope
import com.michaldrabik.network.tmdb.TmdbInterceptor
import com.michaldrabik.network.tmdb.api.TmdbApi
import com.michaldrabik.network.tmdb.api.TmdbService
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
