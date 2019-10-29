package com.michaldrabik.storage.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.michaldrabik.storage.database.dao.ActorsDao
import com.michaldrabik.storage.database.dao.DiscoverShowsDao
import com.michaldrabik.storage.database.dao.EpisodesDao
import com.michaldrabik.storage.database.dao.FollowedShowsDao
import com.michaldrabik.storage.database.dao.ImagesDao
import com.michaldrabik.storage.database.dao.RecentSearchDao
import com.michaldrabik.storage.database.dao.RelatedShowsDao
import com.michaldrabik.storage.database.dao.SeasonsDao
import com.michaldrabik.storage.database.dao.SettingsDao
import com.michaldrabik.storage.database.dao.ShowsDao
import com.michaldrabik.storage.database.dao.UserDao
import com.michaldrabik.storage.database.dao.WatchLaterShowsDao
import com.michaldrabik.storage.database.model.Actor
import com.michaldrabik.storage.database.model.DiscoverShow
import com.michaldrabik.storage.database.model.Episode
import com.michaldrabik.storage.database.model.FollowedShow
import com.michaldrabik.storage.database.model.Image
import com.michaldrabik.storage.database.model.RecentSearch
import com.michaldrabik.storage.database.model.RelatedShow
import com.michaldrabik.storage.database.model.Season
import com.michaldrabik.storage.database.model.Settings
import com.michaldrabik.storage.database.model.Show
import com.michaldrabik.storage.database.model.User
import com.michaldrabik.storage.database.model.WatchLaterShow

const val DATABASE_VERSION = 1
const val DATABASE_NAME = "SHOWLY2_DATABASE"

@Database(
  version = DATABASE_VERSION,
  entities = [
    Show::class,
    DiscoverShow::class,
    FollowedShow::class,
    WatchLaterShow::class,
    RelatedShow::class,
    Image::class,
    User::class,
    Season::class,
    Actor::class,
    Episode::class,
    Settings::class,
    RecentSearch::class]
)
abstract class AppDatabase : RoomDatabase() {

  abstract fun showsDao(): ShowsDao

  abstract fun discoverShowsDao(): DiscoverShowsDao

  abstract fun followedShowsDao(): FollowedShowsDao

  abstract fun watchLaterShowsDao(): WatchLaterShowsDao

  abstract fun relatedShowsDao(): RelatedShowsDao

  abstract fun imagesDao(): ImagesDao

  abstract fun userDao(): UserDao

  abstract fun recentSearchDao(): RecentSearchDao

  abstract fun episodesDao(): EpisodesDao

  abstract fun seasonsDao(): SeasonsDao

  abstract fun actorsDao(): ActorsDao

  abstract fun settingsDao(): SettingsDao
}