package com.michaldrabik.network.di

import com.michaldrabik.network.BuildConfig
import com.michaldrabik.network.Config.TRAKT_BASE_URL
import com.michaldrabik.network.trakt.TraktInterceptor
import com.michaldrabik.network.trakt.api.TraktApi
import com.michaldrabik.network.trakt.api.TraktService
import com.michaldrabik.network.trakt.converters.ShowConverter
import com.michaldrabik.network.trakt.converters.TrendingResultConverter
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@Module
object TraktModule {

  @Provides
  @JvmStatic
  fun providesTraktApi(retrofit: Retrofit): TraktApi =
    TraktApi(retrofit.create(TraktService::class.java))

  @Provides
  @JvmStatic
  fun providesRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit =
    Retrofit.Builder()
      .client(okHttpClient)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .baseUrl(TRAKT_BASE_URL)
      .build()

  @Provides
  @JvmStatic
  fun providesMoshi(): Moshi {
    val showConverter = ShowConverter()
    val trendingResultConverter = TrendingResultConverter(showConverter)

    return Moshi.Builder()
      .add(showConverter)
      .add(trendingResultConverter)
      .build()
  }

  @Provides
  @JvmStatic
  fun providesOkHttp(
    httpLoggingInterceptor: HttpLoggingInterceptor,
    traktInterceptor: TraktInterceptor
  ): OkHttpClient =
    OkHttpClient.Builder()
      .addInterceptor(httpLoggingInterceptor)
      .addInterceptor(traktInterceptor)
      .build()

  @Provides
  @JvmStatic
  fun providesHttpLoggingInterceptor(): HttpLoggingInterceptor =
    HttpLoggingInterceptor().apply {
      level = when {
        BuildConfig.DEBUG -> HttpLoggingInterceptor.Level.BODY
        else -> HttpLoggingInterceptor.Level.NONE
      }
    }

  @Provides
  @JvmStatic
  fun providesTraktInterceptor() = TraktInterceptor()
}