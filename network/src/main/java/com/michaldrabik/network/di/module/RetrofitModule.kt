package com.michaldrabik.network.di.module

import com.michaldrabik.network.BuildConfig
import com.michaldrabik.network.Config.TRAKT_BASE_URL
import com.michaldrabik.network.Config.TVDB_BASE_URL
import com.michaldrabik.network.di.CloudScope
import com.michaldrabik.network.trakt.TraktInterceptor
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit.SECONDS
import javax.inject.Named

@Module
object RetrofitModule {

  private const val TIMEOUT_DURATION = 20L

  @Provides
  @CloudScope
  @Named("retrofitTrakt")
  fun providesTraktRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit =
    Retrofit.Builder()
      .client(okHttpClient)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .baseUrl(TRAKT_BASE_URL)
      .build()

  @Provides
  @CloudScope
  @Named("retrofitTvdb")
  fun providesTvdbRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit =
    Retrofit.Builder()
      .client(okHttpClient)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .baseUrl(TVDB_BASE_URL)
      .build()

  @Provides
  @CloudScope
  fun providesMoshi(): Moshi = Moshi.Builder().build()

  @Provides
  @CloudScope
  fun providesOkHttp(
    httpLoggingInterceptor: HttpLoggingInterceptor,
    traktInterceptor: TraktInterceptor
  ): OkHttpClient =
    OkHttpClient.Builder()
      .writeTimeout(TIMEOUT_DURATION, SECONDS)
      .readTimeout(TIMEOUT_DURATION, SECONDS)
      .callTimeout(TIMEOUT_DURATION, SECONDS)
      .addInterceptor(httpLoggingInterceptor)
      .addInterceptor(traktInterceptor)
      .build()

  @Provides
  @CloudScope
  fun providesHttpLoggingInterceptor(): HttpLoggingInterceptor =
    HttpLoggingInterceptor().apply {
      level = when {
        BuildConfig.DEBUG -> HttpLoggingInterceptor.Level.BODY
        else -> HttpLoggingInterceptor.Level.NONE
      }
    }
}
