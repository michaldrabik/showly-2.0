package com.michaldrabik.data_remote.di.module

import com.michaldrabik.data_remote.reddit.RedditRemoteDataSource
import com.michaldrabik.data_remote.reddit.api.RedditApi
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
  internal fun providesRedditApi(
    authApi: RedditAuthApi,
    listingApi: RedditListingApi
  ): RedditRemoteDataSource =
    RedditApi(authApi, listingApi)

  @Provides
  @Singleton
  internal fun providesRedditAuthApi(@Named("retrofitRedditAuth") retrofit: Retrofit): RedditAuthApi =
    RedditAuthApi(retrofit.create(RedditService::class.java))

  @Provides
  @Singleton
  internal fun providesRedditListingApi(@Named("retrofitRedditListing") retrofit: Retrofit): RedditListingApi =
    RedditListingApi(retrofit.create(RedditService::class.java))
}
