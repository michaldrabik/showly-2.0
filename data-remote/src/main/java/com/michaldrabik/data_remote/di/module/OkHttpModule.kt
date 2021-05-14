package com.michaldrabik.data_remote.di.module

import com.michaldrabik.data_remote.BuildConfig
import com.michaldrabik.data_remote.di.CloudScope
import com.michaldrabik.data_remote.omdb.OmdbInterceptor
import com.michaldrabik.data_remote.tmdb.TmdbInterceptor
import com.michaldrabik.data_remote.trakt.TraktInterceptor
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
  ) = createBaseOkHttpClient()
    .addInterceptor(httpLoggingInterceptor)
    .addInterceptor(traktInterceptor)
    .build()

  @Provides
  @CloudScope
  @Named("okHttpTmdb")
  fun providesTmdbOkHttp(
    httpLoggingInterceptor: HttpLoggingInterceptor,
    tmdbInterceptor: TmdbInterceptor,
  ) = createBaseOkHttpClient()
    .addInterceptor(httpLoggingInterceptor)
    .addInterceptor(tmdbInterceptor)
    .build()

  @Provides
  @CloudScope
  @Named("okHttpOmdb")
  fun providesOmdbOkHttp(
    httpLoggingInterceptor: HttpLoggingInterceptor,
    omdbInterceptor: OmdbInterceptor,
  ) = createBaseOkHttpClient()
    .addInterceptor(httpLoggingInterceptor)
    .addInterceptor(omdbInterceptor)
    .build()

  @Provides
  @CloudScope
  @Named("okHttpAws")
  fun providesAwsOkHttp(
    httpLoggingInterceptor: HttpLoggingInterceptor,
  ) = createBaseOkHttpClient()
    .addInterceptor(httpLoggingInterceptor)
    .build()

  @Provides
  @CloudScope
  @Named("okHttpReddit")
  fun providesRedditOkHttp(
    httpLoggingInterceptor: HttpLoggingInterceptor,
  ) = createBaseOkHttpClient()
    .addInterceptor(httpLoggingInterceptor)
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
