package com.michaldrabik.data_remote.di.module

import com.michaldrabik.data_remote.di.CloudScope
import com.michaldrabik.data_remote.reddit.api.RedditApi
import com.michaldrabik.data_remote.reddit.api.RedditService
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Named

@Module
object RedditModule {

  @Provides
  @CloudScope
  fun providesRedditApi(@Named("retrofitReddit") retrofit: Retrofit): RedditApi =
    RedditApi(retrofit.create(RedditService::class.java))
}
