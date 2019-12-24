package com.michaldrabik.network.di.module

import com.michaldrabik.network.di.CloudScope
import com.michaldrabik.network.trakt.TraktInterceptor
import com.michaldrabik.network.trakt.api.TraktApi
import com.michaldrabik.network.trakt.api.TraktService
import dagger.Module
import dagger.Provides
import javax.inject.Named
import retrofit2.Retrofit

@Module
object TraktModule {

  @Provides
  @CloudScope
  fun providesTraktApi(@Named("retrofitTrakt") retrofit: Retrofit): TraktApi =
    TraktApi(retrofit.create(TraktService::class.java))

  @Provides
  @CloudScope
  fun providesTraktInterceptor() = TraktInterceptor()
}
