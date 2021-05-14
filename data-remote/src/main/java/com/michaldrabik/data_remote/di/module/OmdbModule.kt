package com.michaldrabik.data_remote.di.module

import com.michaldrabik.data_remote.di.CloudScope
import com.michaldrabik.data_remote.omdb.OmdbInterceptor
import com.michaldrabik.data_remote.omdb.api.OmdbApi
import com.michaldrabik.data_remote.omdb.api.OmdbService
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Named

@Module
object OmdbModule {

  @Provides
  @CloudScope
  fun providesOmdbApi(@Named("retrofitOmdb") retrofit: Retrofit): OmdbApi =
    OmdbApi(retrofit.create(OmdbService::class.java))

  @Provides
  @CloudScope
  fun providesOmdbInterceptor() = OmdbInterceptor()
}
