package com.michaldrabik.storage.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.michaldrabik.storage.database.dao.ActorsDao
import com.michaldrabik.storage.database.dao.ArchiveShowsDao
import com.michaldrabik.storage.database.dao.DiscoverShowsDao
import com.michaldrabik.storage.database.dao.EpisodeTranslationsDao
import com.michaldrabik.storage.database.dao.EpisodesDao
import com.michaldrabik.storage.database.dao.EpisodesSyncLogDao
import com.michaldrabik.storage.database.dao.ImagesDao
import com.michaldrabik.storage.database.dao.MyShowsDao
import com.michaldrabik.storage.database.dao.RecentSearchDao
import com.michaldrabik.storage.database.dao.RelatedShowsDao
import com.michaldrabik.storage.database.dao.SeasonsDao
import com.michaldrabik.storage.database.dao.SettingsDao
import com.michaldrabik.storage.database.dao.ShowTranslationsDao
import com.michaldrabik.storage.database.dao.ShowsDao
import com.michaldrabik.storage.database.dao.TraktSyncQueueDao
import com.michaldrabik.storage.database.dao.TranslationsSyncLogDao
import com.michaldrabik.storage.database.dao.UserDao
import com.michaldrabik.storage.database.dao.WatchlistShowsDao
import com.michaldrabik.storage.database.model.Actor
import com.michaldrabik.storage.database.model.ArchiveShow
import com.michaldrabik.storage.database.model.DiscoverShow
import com.michaldrabik.storage.database.model.Episode
import com.michaldrabik.storage.database.model.EpisodeTranslation
import com.michaldrabik.storage.database.model.EpisodesSyncLog
import com.michaldrabik.storage.database.model.Image
import com.michaldrabik.storage.database.model.MyShow
import com.michaldrabik.storage.database.model.RecentSearch
import com.michaldrabik.storage.database.model.RelatedShow
import com.michaldrabik.storage.database.model.Season
import com.michaldrabik.storage.database.model.Settings
import com.michaldrabik.storage.database.model.Show
import com.michaldrabik.storage.database.model.ShowTranslation
import com.michaldrabik.storage.database.model.TraktSyncQueue
import com.michaldrabik.storage.database.model.TranslationsSyncLog
import com.michaldrabik.storage.database.model.User
import com.michaldrabik.storage.database.model.WatchlistShow

@Database(
  version = DATABASE_VERSION,
  entities = [
    Show::class,
    DiscoverShow::class,
    MyShow::class,
    WatchlistShow::class,
    ArchiveShow::class,
    RelatedShow::class,
    Image::class,
    User::class,
    Season::class,
    Actor::class,
    Episode::class,
    Settings::class,
    RecentSearch::class,
    EpisodesSyncLog::class,
    TranslationsSyncLog::class,
    TraktSyncQueue::class,
    ShowTranslation::class,
    EpisodeTranslation::class
  ],
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

  abstract fun showsDao(): ShowsDao

  abstract fun discoverShowsDao(): DiscoverShowsDao

  abstract fun myShowsDao(): MyShowsDao

  abstract fun watchlistShowsDao(): WatchlistShowsDao

  abstract fun archiveShowsDao(): ArchiveShowsDao

  abstract fun relatedShowsDao(): RelatedShowsDao

  abstract fun imagesDao(): ImagesDao

  abstract fun userDao(): UserDao

  abstract fun recentSearchDao(): RecentSearchDao

  abstract fun episodesDao(): EpisodesDao

  abstract fun seasonsDao(): SeasonsDao

  abstract fun actorsDao(): ActorsDao

  abstract fun settingsDao(): SettingsDao

  abstract fun episodesSyncLogDao(): EpisodesSyncLogDao

  abstract fun translationsSyncLogDao(): TranslationsSyncLogDao

  abstract fun traktSyncQueueDao(): TraktSyncQueueDao

  abstract fun showTranslationsDao(): ShowTranslationsDao

  abstract fun episodeTranslationsDao(): EpisodeTranslationsDao
}
