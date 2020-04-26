package com.michaldrabik.network.di.module

import com.michaldrabik.network.di.CloudScope
import com.michaldrabik.network.trakt.converters.CommentConverter
import com.michaldrabik.network.trakt.converters.EpisodeConverter
import com.michaldrabik.network.trakt.converters.SearchResultConverter
import com.michaldrabik.network.trakt.converters.SeasonConverter
import com.michaldrabik.network.trakt.converters.ShowConverter
import com.michaldrabik.network.trakt.converters.SyncProgressItemConverter
import com.michaldrabik.network.trakt.converters.TrendingResultConverter
import com.michaldrabik.network.trakt.converters.UserConverter
import com.michaldrabik.network.tvdb.converters.TvdbImageConverter
import dagger.Module
import dagger.Provides

@Module
object RetrofitConvertersModule {

  @Provides
  @CloudScope
  fun providesShowConverter() = ShowConverter()

  @Provides
  @CloudScope
  fun providesTvdbImageConverter() = TvdbImageConverter()

  @Provides
  @CloudScope
  fun providesEpisodeConverter() = EpisodeConverter()

  @Provides
  @CloudScope
  fun providesSyncProgressItemConverter(
    showConverter: ShowConverter,
    seasonConverter: SeasonConverter
  ) = SyncProgressItemConverter(showConverter, seasonConverter)

  @Provides
  @CloudScope
  fun providesSeasonConverter(episodeConverter: EpisodeConverter) = SeasonConverter(episodeConverter)

  @Provides
  @CloudScope
  fun providesUserConverter() = UserConverter()

  @Provides
  @CloudScope
  fun providesSearchResultConverter(showConverter: ShowConverter) = SearchResultConverter(showConverter)

  @Provides
  @CloudScope
  fun providesCommentsConverter(userConverter: UserConverter) = CommentConverter(userConverter)

  @Provides
  @CloudScope
  fun providesTrendingResultConverter(showConverter: ShowConverter) = TrendingResultConverter(showConverter)
}
