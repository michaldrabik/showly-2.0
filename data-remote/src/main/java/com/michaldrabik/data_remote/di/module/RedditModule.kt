package com.michaldrabik.data_remote.di.module

import com.michaldrabik.data_remote.di.CloudScope
import com.michaldrabik.data_remote.reddit.RedditInterceptor
import com.michaldrabik.data_remote.reddit.api.RedditAuthApi
import com.michaldrabik.data_remote.reddit.api.RedditListingApi
import com.michaldrabik.data_remote.reddit.api.RedditService
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Named

@Module
object RedditModule {

  @Provides
  @CloudScope
  fun providesRedditAuthApi(@Named("retrofitRedditAuth") retrofit: Retrofit): RedditAuthApi =
    RedditAuthApi(retrofit.create(RedditService::class.java))

  @Provides
  @CloudScope
  fun providesRedditListingApi(@Named("retrofitRedditListing") retrofit: Retrofit): RedditListingApi =
    RedditListingApi(retrofit.create(RedditService::class.java))

  @Provides
  @CloudScope
  fun providesRedditInterceptor() = RedditInterceptor()
}
