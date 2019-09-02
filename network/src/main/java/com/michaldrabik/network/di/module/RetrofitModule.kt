package com.michaldrabik.network.di.module

import com.michaldrabik.network.BuildConfig
import com.michaldrabik.network.Config.TRAKT_BASE_URL
import com.michaldrabik.network.Config.TVDB_BASE_URL
import com.michaldrabik.network.trakt.TraktInterceptor
import com.michaldrabik.network.trakt.converters.EpisodeConverter
import com.michaldrabik.network.trakt.converters.ShowConverter
import com.michaldrabik.network.trakt.converters.TrendingResultConverter
import com.michaldrabik.network.tvdb.converters.TvdbImageConverter
import com.michaldrabik.network.tvdb.converters.TvdbImagesResultConverter
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Named

@Module
object RetrofitModule {

  @Provides
  @Named("retrofitTrakt")
  @JvmStatic
  fun providesTraktRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit =
    Retrofit.Builder()
      .client(okHttpClient)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .baseUrl(TRAKT_BASE_URL)
      .build()

  @Provides
  @Named("retrofitTvdb")
  @JvmStatic
  fun providesTvdbRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit =
    Retrofit.Builder()
      .client(okHttpClient)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .baseUrl(TVDB_BASE_URL)
      .build()

  @Provides
  @JvmStatic
  fun providesMoshi(): Moshi {
    val showConverter = ShowConverter()
    val trendingResultConverter = TrendingResultConverter(showConverter)
    val tvdbImageConverter = TvdbImageConverter()
    val tvdbImageResultConverter = TvdbImagesResultConverter(tvdbImageConverter)
    val episodeConverter = EpisodeConverter()

    return Moshi.Builder()
      .add(showConverter)
      .add(trendingResultConverter)
      .add(tvdbImageConverter)
      .add(tvdbImageResultConverter)
      .add(episodeConverter)
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
}