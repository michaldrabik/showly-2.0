package com.michaldrabik.network.di.module

import com.michaldrabik.network.BuildConfig
import com.michaldrabik.network.di.CloudScope
import com.michaldrabik.network.tmdb.TmdbInterceptor
import com.michaldrabik.network.trakt.TraktInterceptor
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit.SECONDS
import javax.inject.Named

@Module
object OkHttpModule {

  private const val TIMEOUT_DURATION = 20L

  @Provides
  @CloudScope
  @Named("okHttpTrakt")
  fun providesTraktOkHttp(
    httpLoggingInterceptor: HttpLoggingInterceptor,
    traktInterceptor: TraktInterceptor,
    tmdbInterceptor: TmdbInterceptor
  ) = createBaseOkHttpClient()
    .addInterceptor(httpLoggingInterceptor)
    .addInterceptor(traktInterceptor)
    .addInterceptor(tmdbInterceptor)
    .build()

  @Provides
  @CloudScope
  @Named("okHttpTvdb")
  fun providesTvdbOkHttp(
    httpLoggingInterceptor: HttpLoggingInterceptor
  ) = createBaseOkHttpClient()
    .addInterceptor(httpLoggingInterceptor)
    .build()

  @Provides
  @CloudScope
  @Named("okHttpTmdb")
  fun providesTmdbOkHttp(
    httpLoggingInterceptor: HttpLoggingInterceptor,
    tmdbInterceptor: TmdbInterceptor
  ) = createBaseOkHttpClient()
    .addInterceptor(httpLoggingInterceptor)
    .addInterceptor(tmdbInterceptor)
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

  private fun createBaseOkHttpClient() = OkHttpClient.Builder()
    .writeTimeout(TIMEOUT_DURATION, SECONDS)
    .readTimeout(TIMEOUT_DURATION, SECONDS)
    .callTimeout(TIMEOUT_DURATION, SECONDS)
}
