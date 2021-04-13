package com.michaldrabik.data_remote.di.module

import com.michaldrabik.data_remote.di.CloudScope
import com.michaldrabik.data_remote.trakt.TraktInterceptor
import com.michaldrabik.data_remote.trakt.api.TraktApi
import com.michaldrabik.data_remote.trakt.api.TraktService
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Named

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
