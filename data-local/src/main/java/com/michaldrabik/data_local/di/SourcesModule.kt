package com.michaldrabik.data_local.di

import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_local.sources.ArchiveMoviesLocalDataSource
import com.michaldrabik.data_local.sources.ArchiveShowsLocalDataSource
import com.michaldrabik.data_local.sources.CustomImagesLocalDataSource
import com.michaldrabik.data_local.sources.CustomListsItemsLocalDataSource
import com.michaldrabik.data_local.sources.CustomListsLocalDataSource
import com.michaldrabik.data_local.sources.DiscoverMoviesLocalDataSource
import com.michaldrabik.data_local.sources.DiscoverShowsLocalDataSource
import com.michaldrabik.data_local.sources.EpisodeTranslationsLocalDataSource
import com.michaldrabik.data_local.sources.EpisodesLocalDataSource
import com.michaldrabik.data_local.sources.EpisodesSyncLogLocalDataSource
import com.michaldrabik.data_local.sources.MoviesLocalDataSource
import com.michaldrabik.data_local.sources.ShowsLocalDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class SourcesModule {

  @Provides
  @Singleton
  fun providesShows(database: AppDatabase): ShowsLocalDataSource =
    database.showsDao()

  @Provides
  @Singleton
  fun providesMovies(database: AppDatabase): MoviesLocalDataSource =
    database.moviesDao()

  @Provides
  @Singleton
  fun providesArchivesShows(database: AppDatabase): ArchiveShowsLocalDataSource =
    database.archiveShowsDao()

  @Provides
  @Singleton
  fun providesArchivesMovies(database: AppDatabase): ArchiveMoviesLocalDataSource =
    database.archiveMoviesDao()

  @Provides
  @Singleton
  fun providesCustomListsItems(database: AppDatabase): CustomListsItemsLocalDataSource =
    database.customListsItemsDao()

  @Provides
  @Singleton
  fun providesCustomImages(database: AppDatabase): CustomImagesLocalDataSource =
    database.customImagesDao()

  @Provides
  @Singleton
  fun providesCustomLists(database: AppDatabase): CustomListsLocalDataSource =
    database.customListsDao()

  @Provides
  @Singleton
  fun providesDiscoverShows(database: AppDatabase): DiscoverShowsLocalDataSource =
    database.discoverShowsDao()

  @Provides
  @Singleton
  fun providesDiscoverMovies(database: AppDatabase): DiscoverMoviesLocalDataSource =
    database.discoverMoviesDao()

  @Provides
  @Singleton
  fun providesEpisodes(database: AppDatabase): EpisodesLocalDataSource =
    database.episodesDao()

  @Provides
  @Singleton
  fun providesEpisodesSyncLog(database: AppDatabase): EpisodesSyncLogLocalDataSource =
    database.episodesSyncLogDao()

  @Provides
  @Singleton
  fun providesEpisodesTranslations(database: AppDatabase): EpisodeTranslationsLocalDataSource =
    database.episodeTranslationsDao()
}
