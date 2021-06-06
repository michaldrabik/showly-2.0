package com.michaldrabik.data_remote.di.module

import com.michaldrabik.data_remote.Config.AWS_BASE_URL
import com.michaldrabik.data_remote.Config.OMDB_BASE_URL
import com.michaldrabik.data_remote.Config.REDDIT_BASE_URL
import com.michaldrabik.data_remote.Config.REDDIT_OAUTH_BASE_URL
import com.michaldrabik.data_remote.Config.TMDB_BASE_URL
import com.michaldrabik.data_remote.Config.TRAKT_BASE_URL
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {

  @Provides
  @Singleton
  @Named("retrofitTrakt")
  fun providesTraktRetrofit(
    @Named("okHttpTrakt") okHttpClient: OkHttpClient,
    moshi: Moshi,
  ): Retrofit =
    Retrofit.Builder()
      .client(okHttpClient)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .baseUrl(TRAKT_BASE_URL)
      .build()

  @Provides
  @Singleton
  @Named("retrofitTmdb")
  fun providesTmdbRetrofit(
    @Named("okHttpTmdb") okHttpClient: OkHttpClient,
    moshi: Moshi,
  ): Retrofit =
    Retrofit.Builder()
      .client(okHttpClient)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .baseUrl(TMDB_BASE_URL)
      .build()

  @Provides
  @Singleton
  @Named("retrofitOmdb")
  fun providesOmdbRetrofit(
    @Named("okHttpOmdb") okHttpClient: OkHttpClient,
    moshi: Moshi,
  ): Retrofit =
    Retrofit.Builder()
      .client(okHttpClient)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .baseUrl(OMDB_BASE_URL)
      .build()

  @Provides
  @Singleton
  @Named("retrofitAws")
  fun providesAwsRetrofit(
    @Named("okHttpAws") okHttpClient: OkHttpClient,
    moshi: Moshi,
  ): Retrofit =
    Retrofit.Builder()
      .client(okHttpClient)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .baseUrl(AWS_BASE_URL)
      .build()

  @Provides
  @Singleton
  @Named("retrofitRedditAuth")
  fun providesRedditRetrofit(
    @Named("okHttpReddit") okHttpClient: OkHttpClient,
    moshi: Moshi,
  ): Retrofit =
    Retrofit.Builder()
      .client(okHttpClient)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .baseUrl(REDDIT_BASE_URL)
      .build()

  @Provides
  @Singleton
  @Named("retrofitRedditListing")
  fun providesRedditRetrofitOAuth(
    @Named("okHttpReddit") okHttpClient: OkHttpClient,
    moshiConverter: MoshiConverterFactory,
  ): Retrofit =
    Retrofit.Builder()
      .client(okHttpClient)
      .addConverterFactory(moshiConverter)
      .baseUrl(REDDIT_OAUTH_BASE_URL)
      .build()

  @Provides
  @Singleton
  fun providesMoshiFactory(moshi: Moshi): MoshiConverterFactory = MoshiConverterFactory.create(moshi)

  @Provides
  @Singleton
  fun providesMoshi(): Moshi = Moshi.Builder().build()
}
