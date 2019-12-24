package com.michaldrabik.network.di.module

import com.michaldrabik.network.di.CloudScope
import com.michaldrabik.network.tvdb.api.TvdbApi
import com.michaldrabik.network.tvdb.api.TvdbService
import dagger.Module
import dagger.Provides
import javax.inject.Named
import retrofit2.Retrofit

@Module
object TvdbModule {

  @Provides
  @CloudScope
  fun providesTvdbApi(@Named("retrofitTvdb") retrofit: Retrofit): TvdbApi =
    TvdbApi(retrofit.create(TvdbService::class.java))
}
