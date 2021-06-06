package com.michaldrabik.data_remote.di.module

import com.michaldrabik.data_remote.reddit.api.RedditAuthApi
import com.michaldrabik.data_remote.reddit.api.RedditListingApi
import com.michaldrabik.data_remote.reddit.api.RedditService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RedditModule {

  @Provides
  @Singleton
  fun providesRedditAuthApi(@Named("retrofitRedditAuth") retrofit: Retrofit): RedditAuthApi =
    RedditAuthApi(retrofit.create(RedditService::class.java))

  @Provides
  @Singleton
  fun providesRedditListingApi(@Named("retrofitRedditListing") retrofit: Retrofit): RedditListingApi =
    RedditListingApi(retrofit.create(RedditService::class.java))
}
