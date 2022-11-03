package com.michaldrabik.data_remote.di.module

import com.michaldrabik.data_remote.BuildConfig
import com.michaldrabik.data_remote.omdb.OmdbInterceptor
import com.michaldrabik.data_remote.tmdb.TmdbInterceptor
import com.michaldrabik.data_remote.trakt.interceptors.TraktAuthenticator
import com.michaldrabik.data_remote.trakt.interceptors.TraktAuthorizationInterceptor
import com.michaldrabik.data_remote.trakt.interceptors.TraktHeadersInterceptor
import com.michaldrabik.data_remote.trakt.interceptors.TraktRefreshTokenInterceptor
import com.michaldrabik.data_remote.trakt.interceptors.TraktRetryInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit.SECONDS
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OkHttpModule {

  private const val TIMEOUT_DURATION = 20L

  @Provides
  @Singleton
  @Named("okHttpBase")
  fun providesBaseOkHttp(): OkHttpClient {
    return createBaseOkHttpClient().build()
  }

  @Provides
  @Singleton
  @Named("okHttpTrakt")
  fun providesTraktOkHttp(
    httpLoggingInterceptor: HttpLoggingInterceptor,
    traktAuthorizationInterceptor: TraktAuthorizationInterceptor,
    traktHeadersInterceptor: TraktHeadersInterceptor,
    traktRefreshTokenInterceptor: TraktRefreshTokenInterceptor,
    traktRetryInterceptor: TraktRetryInterceptor,
    traktAuthenticator: TraktAuthenticator,
  ): OkHttpClient {
    return createBaseOkHttpClient()
      .addInterceptor(traktHeadersInterceptor)
      .addInterceptor(traktRefreshTokenInterceptor)
      .addInterceptor(traktAuthorizationInterceptor)
      .addInterceptor(traktRetryInterceptor)
      .addInterceptor(httpLoggingInterceptor)
      .authenticator(traktAuthenticator)
      .build()
  }

  @Provides
  @Singleton
  @Named("okHttpTmdb")
  fun providesTmdbOkHttp(
    httpLoggingInterceptor: HttpLoggingInterceptor,
    tmdbInterceptor: TmdbInterceptor,
  ) = createBaseOkHttpClient()
    .addInterceptor(httpLoggingInterceptor)
    .addInterceptor(tmdbInterceptor)
    .build()

  @Provides
  @Singleton
  @Named("okHttpOmdb")
  fun providesOmdbOkHttp(
    httpLoggingInterceptor: HttpLoggingInterceptor,
    omdbInterceptor: OmdbInterceptor,
  ) = createBaseOkHttpClient()
    .addInterceptor(httpLoggingInterceptor)
    .addInterceptor(omdbInterceptor)
    .build()

  @Provides
  @Singleton
  @Named("okHttpAws")
  fun providesAwsOkHttp(
    httpLoggingInterceptor: HttpLoggingInterceptor,
  ) = createBaseOkHttpClient()
    .addInterceptor(httpLoggingInterceptor)
    .build()

  @Provides
  @Singleton
  @Named("okHttpReddit")
  fun providesRedditOkHttp(
    httpLoggingInterceptor: HttpLoggingInterceptor,
  ) = createBaseOkHttpClient()
    .addInterceptor(httpLoggingInterceptor)
    .build()

  @Provides
  @Singleton
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
