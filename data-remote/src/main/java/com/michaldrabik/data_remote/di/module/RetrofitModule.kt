package com.michaldrabik.data_remote.di.module

import com.michaldrabik.data_remote.Config.AWS_BASE_URL
import com.michaldrabik.data_remote.Config.OMDB_BASE_URL
import com.michaldrabik.data_remote.Config.REDDIT_BASE_URL
import com.michaldrabik.data_remote.Config.REDDIT_OAUTH_BASE_URL
import com.michaldrabik.data_remote.Config.TMDB_BASE_URL
import com.michaldrabik.data_remote.Config.TRAKT_BASE_URL
import com.michaldrabik.data_remote.di.CloudScope
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Named

@Module
object RetrofitModule {

  @Provides
  @CloudScope
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
  @CloudScope
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
  @CloudScope
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
  @CloudScope
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
  @CloudScope
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
  @CloudScope
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
  @CloudScope
  fun providesMoshiFactory(moshi: Moshi): MoshiConverterFactory = MoshiConverterFactory.create(moshi)

  @Provides
  @CloudScope
  fun providesMoshi(): Moshi = Moshi.Builder().build()
}
