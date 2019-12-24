package com.michaldrabik.network.di.module

import com.michaldrabik.network.BuildConfig
import com.michaldrabik.network.Config.TRAKT_BASE_URL
import com.michaldrabik.network.Config.TVDB_BASE_URL
import com.michaldrabik.network.di.CloudScope
import com.michaldrabik.network.trakt.TraktInterceptor
import com.michaldrabik.network.trakt.converters.CommentConverter
import com.michaldrabik.network.trakt.converters.EpisodeConverter
import com.michaldrabik.network.trakt.converters.SearchResultConverter
import com.michaldrabik.network.trakt.converters.SeasonConverter
import com.michaldrabik.network.trakt.converters.ShowConverter
import com.michaldrabik.network.trakt.converters.SyncProgressItemConverter
import com.michaldrabik.network.trakt.converters.TrendingResultConverter
import com.michaldrabik.network.trakt.converters.UserConverter
import com.michaldrabik.network.tvdb.converters.TvdbActorConverter
import com.michaldrabik.network.tvdb.converters.TvdbImageConverter
import com.michaldrabik.network.tvdb.converters.TvdbResultConverter
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import javax.inject.Named
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@Module
object RetrofitModule {

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

  // TODO Refactor into binding map
  @Provides
  @CloudScope
  fun providesMoshi(): Moshi {
    val showConverter = ShowConverter()
    val trendingResultConverter = TrendingResultConverter(showConverter)
    val searchResultConverter = SearchResultConverter(showConverter)

    val tvdbImageConverter = TvdbImageConverter()
    val tvdbImageResultConverter = TvdbResultConverter(tvdbImageConverter)

    val actorConverter = TvdbActorConverter()
    val actorResultConverter = TvdbResultConverter(actorConverter)

    val episodeConverter = EpisodeConverter()
    val seasonConverter = SeasonConverter(episodeConverter)

    val syncProgressItemConverter = SyncProgressItemConverter(showConverter, seasonConverter)

    val userConverter = UserConverter()
    val commentsConverter = CommentConverter(userConverter)

    return Moshi.Builder()
      .add(showConverter)
      .add(trendingResultConverter)
      .add(tvdbImageConverter)
      .add(tvdbImageResultConverter)
      .add(episodeConverter)
      .add(seasonConverter)
      .add(searchResultConverter)
      .add(actorConverter)
      .add(actorResultConverter)
      .add(syncProgressItemConverter)
      .add(userConverter)
      .add(commentsConverter)
      .build()
  }

  @Provides
  @CloudScope
  fun providesOkHttp(
    httpLoggingInterceptor: HttpLoggingInterceptor,
    traktInterceptor: TraktInterceptor
  ): OkHttpClient =
    OkHttpClient.Builder()
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
