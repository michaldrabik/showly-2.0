package com.michaldrabik.storage.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.michaldrabik.storage.database.dao.ActorsDao
import com.michaldrabik.storage.database.dao.ArchiveShowsDao
import com.michaldrabik.storage.database.dao.DiscoverMoviesDao
import com.michaldrabik.storage.database.dao.DiscoverShowsDao
import com.michaldrabik.storage.database.dao.EpisodeTranslationsDao
import com.michaldrabik.storage.database.dao.EpisodesDao
import com.michaldrabik.storage.database.dao.EpisodesSyncLogDao
import com.michaldrabik.storage.database.dao.MovieImagesDao
import com.michaldrabik.storage.database.dao.MovieTranslationsDao
import com.michaldrabik.storage.database.dao.MoviesDao
import com.michaldrabik.storage.database.dao.MoviesSyncLogDao
import com.michaldrabik.storage.database.dao.MyMoviesDao
import com.michaldrabik.storage.database.dao.MyShowsDao
import com.michaldrabik.storage.database.dao.RecentSearchDao
import com.michaldrabik.storage.database.dao.RelatedMoviesDao
import com.michaldrabik.storage.database.dao.RelatedShowsDao
import com.michaldrabik.storage.database.dao.SeasonsDao
import com.michaldrabik.storage.database.dao.SettingsDao
import com.michaldrabik.storage.database.dao.ShowImagesDao
import com.michaldrabik.storage.database.dao.ShowTranslationsDao
import com.michaldrabik.storage.database.dao.ShowsDao
import com.michaldrabik.storage.database.dao.TraktSyncLogDao
import com.michaldrabik.storage.database.dao.TraktSyncQueueDao
import com.michaldrabik.storage.database.dao.TranslationsMoviesSyncLogDao
import com.michaldrabik.storage.database.dao.TranslationsSyncLogDao
import com.michaldrabik.storage.database.dao.UserDao
import com.michaldrabik.storage.database.dao.WatchlistMoviesDao
import com.michaldrabik.storage.database.dao.WatchlistShowsDao
import com.michaldrabik.storage.database.migrations.DATABASE_VERSION
import com.michaldrabik.storage.database.model.Actor
import com.michaldrabik.storage.database.model.ArchiveShow
import com.michaldrabik.storage.database.model.DiscoverMovie
import com.michaldrabik.storage.database.model.DiscoverShow
import com.michaldrabik.storage.database.model.Episode
import com.michaldrabik.storage.database.model.EpisodeTranslation
import com.michaldrabik.storage.database.model.EpisodesSyncLog
import com.michaldrabik.storage.database.model.Movie
import com.michaldrabik.storage.database.model.MovieImage
import com.michaldrabik.storage.database.model.MovieTranslation
import com.michaldrabik.storage.database.model.MoviesSyncLog
import com.michaldrabik.storage.database.model.MyMovie
import com.michaldrabik.storage.database.model.MyShow
import com.michaldrabik.storage.database.model.RecentSearch
import com.michaldrabik.storage.database.model.RelatedMovie
import com.michaldrabik.storage.database.model.RelatedShow
import com.michaldrabik.storage.database.model.Season
import com.michaldrabik.storage.database.model.Settings
import com.michaldrabik.storage.database.model.Show
import com.michaldrabik.storage.database.model.ShowImage
import com.michaldrabik.storage.database.model.ShowTranslation
import com.michaldrabik.storage.database.model.TraktSyncLog
import com.michaldrabik.storage.database.model.TraktSyncQueue
import com.michaldrabik.storage.database.model.TranslationsMoviesSyncLog
import com.michaldrabik.storage.database.model.TranslationsSyncLog
import com.michaldrabik.storage.database.model.User
import com.michaldrabik.storage.database.model.WatchlistMovie
import com.michaldrabik.storage.database.model.WatchlistShow

@Database(
  version = DATABASE_VERSION,
  entities = [
    Show::class,
    Movie::class,
    DiscoverShow::class,
    DiscoverMovie::class,
    MyShow::class,
    MyMovie::class,
    WatchlistShow::class,
    WatchlistMovie::class,
    ArchiveShow::class,
    RelatedShow::class,
    RelatedMovie::class,
    ShowImage::class,
    MovieImage::class,
    User::class,
    Season::class,
    Actor::class,
    Episode::class,
    Settings::class,
    RecentSearch::class,
    EpisodesSyncLog::class,
    MoviesSyncLog::class,
    TranslationsSyncLog::class,
    TranslationsMoviesSyncLog::class,
    TraktSyncQueue::class,
    TraktSyncLog::class,
    ShowTranslation::class,
    MovieTranslation::class,
    EpisodeTranslation::class
  ],
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

  abstract fun showsDao(): ShowsDao

  abstract fun moviesDao(): MoviesDao

  abstract fun discoverShowsDao(): DiscoverShowsDao

  abstract fun discoverMoviesDao(): DiscoverMoviesDao

  abstract fun myShowsDao(): MyShowsDao

  abstract fun myMoviesDao(): MyMoviesDao

  abstract fun watchlistShowsDao(): WatchlistShowsDao

  abstract fun watchlistMoviesDao(): WatchlistMoviesDao

  abstract fun archiveShowsDao(): ArchiveShowsDao

  abstract fun relatedShowsDao(): RelatedShowsDao

  abstract fun relatedMoviesDao(): RelatedMoviesDao

  abstract fun showImagesDao(): ShowImagesDao

  abstract fun movieImagesDao(): MovieImagesDao

  abstract fun userDao(): UserDao

  abstract fun recentSearchDao(): RecentSearchDao

  abstract fun episodesDao(): EpisodesDao

  abstract fun seasonsDao(): SeasonsDao

  abstract fun actorsDao(): ActorsDao

  abstract fun settingsDao(): SettingsDao

  abstract fun traktSyncLogDao(): TraktSyncLogDao

  abstract fun moviesSyncLogDao(): MoviesSyncLogDao

  abstract fun episodesSyncLogDao(): EpisodesSyncLogDao

  abstract fun translationsSyncLogDao(): TranslationsSyncLogDao

  abstract fun translationsMoviesSyncLogDao(): TranslationsMoviesSyncLogDao

  abstract fun traktSyncQueueDao(): TraktSyncQueueDao

  abstract fun showTranslationsDao(): ShowTranslationsDao

  abstract fun movieTranslationsDao(): MovieTranslationsDao

  abstract fun episodeTranslationsDao(): EpisodeTranslationsDao
}
