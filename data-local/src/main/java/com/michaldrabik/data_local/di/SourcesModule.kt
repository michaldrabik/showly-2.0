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
import com.michaldrabik.data_local.sources.MovieImagesLocalDataSource
import com.michaldrabik.data_local.sources.MovieRatingsLocalDataSource
import com.michaldrabik.data_local.sources.MovieStreamingsLocalDataSource
import com.michaldrabik.data_local.sources.MovieTranslationsLocalDataSource
import com.michaldrabik.data_local.sources.MoviesLocalDataSource
import com.michaldrabik.data_local.sources.MoviesSyncLogLocalDataSource
import com.michaldrabik.data_local.sources.MyMoviesLocalDataSource
import com.michaldrabik.data_local.sources.MyShowsLocalDataSource
import com.michaldrabik.data_local.sources.NewsLocalDataSource
import com.michaldrabik.data_local.sources.PeopleCreditsLocalDataSource
import com.michaldrabik.data_local.sources.PeopleImagesLocalDataSource
import com.michaldrabik.data_local.sources.PeopleLocalDataSource
import com.michaldrabik.data_local.sources.PeopleShowsMoviesLocalDataSource
import com.michaldrabik.data_local.sources.RatingsLocalDataSource
import com.michaldrabik.data_local.sources.RecentSearchLocalDataSource
import com.michaldrabik.data_local.sources.RelatedMoviesLocalDataSource
import com.michaldrabik.data_local.sources.RelatedShowsLocalDataSource
import com.michaldrabik.data_local.sources.SeasonsLocalDataSource
import com.michaldrabik.data_local.sources.SettingsLocalDataSource
import com.michaldrabik.data_local.sources.ShowImagesLocalDataSource
import com.michaldrabik.data_local.sources.ShowRatingsLocalDataSource
import com.michaldrabik.data_local.sources.ShowStreamingsLocalDataSource
import com.michaldrabik.data_local.sources.ShowTranslationsLocalDataSource
import com.michaldrabik.data_local.sources.ShowsLocalDataSource
import com.michaldrabik.data_local.sources.TraktSyncLogLocalDataSource
import com.michaldrabik.data_local.sources.TraktSyncQueueLocalDataSource
import com.michaldrabik.data_local.sources.TranslationsMoviesSyncLogLocalDataSource
import com.michaldrabik.data_local.sources.TranslationsShowsSyncLogLocalDataSource
import com.michaldrabik.data_local.sources.UserLocalDataSource
import com.michaldrabik.data_local.sources.WatchlistMoviesLocalDataSource
import com.michaldrabik.data_local.sources.WatchlistShowsLocalDataSource
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
  internal fun providesShows(database: AppDatabase): ShowsLocalDataSource =
    database.showsDao()

  @Provides
  @Singleton
  internal fun providesMovies(database: AppDatabase): MoviesLocalDataSource =
    database.moviesDao()

  @Provides
  @Singleton
  internal fun providesArchivesShows(database: AppDatabase): ArchiveShowsLocalDataSource =
    database.archiveShowsDao()

  @Provides
  @Singleton
  internal fun providesArchivesMovies(database: AppDatabase): ArchiveMoviesLocalDataSource =
    database.archiveMoviesDao()

  @Provides
  @Singleton
  internal fun providesCustomListsItems(database: AppDatabase): CustomListsItemsLocalDataSource =
    database.customListsItemsDao()

  @Provides
  @Singleton
  internal fun providesCustomImages(database: AppDatabase): CustomImagesLocalDataSource =
    database.customImagesDao()

  @Provides
  @Singleton
  internal fun providesCustomLists(database: AppDatabase): CustomListsLocalDataSource =
    database.customListsDao()

  @Provides
  @Singleton
  internal fun providesDiscoverShows(database: AppDatabase): DiscoverShowsLocalDataSource =
    database.discoverShowsDao()

  @Provides
  @Singleton
  internal fun providesDiscoverMovies(database: AppDatabase): DiscoverMoviesLocalDataSource =
    database.discoverMoviesDao()

  @Provides
  @Singleton
  internal fun providesEpisodes(database: AppDatabase): EpisodesLocalDataSource =
    database.episodesDao()

  @Provides
  @Singleton
  internal fun providesEpisodesSyncLog(database: AppDatabase): EpisodesSyncLogLocalDataSource =
    database.episodesSyncLogDao()

  @Provides
  @Singleton
  internal fun providesEpisodesTranslations(database: AppDatabase): EpisodeTranslationsLocalDataSource =
    database.episodeTranslationsDao()

  @Provides
  @Singleton
  internal fun providesMovieImages(database: AppDatabase): MovieImagesLocalDataSource =
    database.movieImagesDao()

  @Provides
  @Singleton
  internal fun providesMovieRatings(database: AppDatabase): MovieRatingsLocalDataSource =
    database.movieRatingsDao()

  @Provides
  @Singleton
  internal fun providesMovieSyncLog(database: AppDatabase): MoviesSyncLogLocalDataSource =
    database.moviesSyncLogDao()

  @Provides
  @Singleton
  internal fun providesMovieStreaming(database: AppDatabase): MovieStreamingsLocalDataSource =
    database.movieStreamingsDao()

  @Provides
  @Singleton
  internal fun providesMovieTranslation(database: AppDatabase): MovieTranslationsLocalDataSource =
    database.movieTranslationsDao()

  @Provides
  @Singleton
  internal fun providesMyMovies(database: AppDatabase): MyMoviesLocalDataSource =
    database.myMoviesDao()

  @Provides
  @Singleton
  internal fun providesMyShows(database: AppDatabase): MyShowsLocalDataSource =
    database.myShowsDao()

  @Provides
  @Singleton
  internal fun providesNews(database: AppDatabase): NewsLocalDataSource =
    database.newsDao()

  @Provides
  @Singleton
  internal fun providesPeopleCredits(database: AppDatabase): PeopleCreditsLocalDataSource =
    database.peopleCreditsDao()

  @Provides
  @Singleton
  internal fun providesPeople(database: AppDatabase): PeopleLocalDataSource =
    database.peopleDao()

  @Provides
  @Singleton
  internal fun providesPeopleImages(database: AppDatabase): PeopleImagesLocalDataSource =
    database.peopleImagesDao()

  @Provides
  @Singleton
  internal fun providesPeopleShowsMovies(database: AppDatabase): PeopleShowsMoviesLocalDataSource =
    database.peopleShowsMoviesDao()

  @Provides
  @Singleton
  internal fun providesRatings(database: AppDatabase): RatingsLocalDataSource =
    database.ratingsDao()

  @Provides
  @Singleton
  internal fun providesRecentSearch(database: AppDatabase): RecentSearchLocalDataSource =
    database.recentSearchDao()

  @Provides
  @Singleton
  internal fun providesSeasons(database: AppDatabase): SeasonsLocalDataSource =
    database.seasonsDao()

  @Provides
  @Singleton
  internal fun providesSettings(database: AppDatabase): SettingsLocalDataSource =
    database.settingsDao()

  @Provides
  @Singleton
  internal fun providesShowImages(database: AppDatabase): ShowImagesLocalDataSource =
    database.showImagesDao()

  @Provides
  @Singleton
  internal fun providesShowRatings(database: AppDatabase): ShowRatingsLocalDataSource =
    database.showRatingsDao()

  @Provides
  @Singleton
  internal fun providesShowStreamings(database: AppDatabase): ShowStreamingsLocalDataSource =
    database.showStreamingsDao()

  @Provides
  @Singleton
  internal fun providesShowTranslations(database: AppDatabase): ShowTranslationsLocalDataSource =
    database.showTranslationsDao()

  @Provides
  @Singleton
  internal fun providesTraktSyncLog(database: AppDatabase): TraktSyncLogLocalDataSource =
    database.traktSyncLogDao()

  @Provides
  @Singleton
  internal fun providesTraktSyncQueue(database: AppDatabase): TraktSyncQueueLocalDataSource =
    database.traktSyncQueueDao()

  @Provides
  @Singleton
  internal fun providesTranslationsMovies(database: AppDatabase): TranslationsMoviesSyncLogLocalDataSource =
    database.translationsMoviesSyncLogDao()

  @Provides
  @Singleton
  internal fun providesTranslationsShows(database: AppDatabase): TranslationsShowsSyncLogLocalDataSource =
    database.translationsSyncLogDao()

  @Provides
  @Singleton
  internal fun providesUser(database: AppDatabase): UserLocalDataSource =
    database.userDao()

  @Provides
  @Singleton
  internal fun providesWatchlistMovies(database: AppDatabase): WatchlistMoviesLocalDataSource =
    database.watchlistMoviesDao()

  @Provides
  @Singleton
  internal fun providesWatchlistShows(database: AppDatabase): WatchlistShowsLocalDataSource =
    database.watchlistShowsDao()

  @Provides
  @Singleton
  internal fun providesRelatedMovies(database: AppDatabase): RelatedMoviesLocalDataSource =
    database.relatedMoviesDao()

  @Provides
  @Singleton
  internal fun providesRelatedShows(database: AppDatabase): RelatedShowsLocalDataSource =
    database.relatedShowsDao()
}
