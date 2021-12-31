package com.michaldrabik.data_local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.michaldrabik.data_local.database.dao.ArchiveMoviesDao
import com.michaldrabik.data_local.database.dao.ArchiveShowsDao
import com.michaldrabik.data_local.database.dao.CustomImagesDao
import com.michaldrabik.data_local.database.dao.CustomListsDao
import com.michaldrabik.data_local.database.dao.CustomListsItemsDao
import com.michaldrabik.data_local.database.dao.DiscoverMoviesDao
import com.michaldrabik.data_local.database.dao.DiscoverShowsDao
import com.michaldrabik.data_local.database.dao.EpisodeTranslationsDao
import com.michaldrabik.data_local.database.dao.EpisodesDao
import com.michaldrabik.data_local.database.dao.EpisodesSyncLogDao
import com.michaldrabik.data_local.database.dao.MovieImagesDao
import com.michaldrabik.data_local.database.dao.MovieRatingsDao
import com.michaldrabik.data_local.database.dao.MovieStreamingsDao
import com.michaldrabik.data_local.database.dao.MovieTranslationsDao
import com.michaldrabik.data_local.database.dao.MoviesDao
import com.michaldrabik.data_local.database.dao.MoviesSyncLogDao
import com.michaldrabik.data_local.database.dao.MyMoviesDao
import com.michaldrabik.data_local.database.dao.MyShowsDao
import com.michaldrabik.data_local.database.dao.NewsDao
import com.michaldrabik.data_local.database.dao.PeopleCreditsDao
import com.michaldrabik.data_local.database.dao.PeopleDao
import com.michaldrabik.data_local.database.dao.PeopleImagesDao
import com.michaldrabik.data_local.database.dao.PeopleShowsMoviesDao
import com.michaldrabik.data_local.database.dao.RatingsDao
import com.michaldrabik.data_local.database.dao.RecentSearchDao
import com.michaldrabik.data_local.database.dao.RelatedMoviesDao
import com.michaldrabik.data_local.database.dao.RelatedShowsDao
import com.michaldrabik.data_local.database.dao.SeasonsDao
import com.michaldrabik.data_local.database.dao.SettingsDao
import com.michaldrabik.data_local.database.dao.ShowImagesDao
import com.michaldrabik.data_local.database.dao.ShowRatingsDao
import com.michaldrabik.data_local.database.dao.ShowStreamingsDao
import com.michaldrabik.data_local.database.dao.ShowTranslationsDao
import com.michaldrabik.data_local.database.dao.ShowsDao
import com.michaldrabik.data_local.database.dao.TraktSyncLogDao
import com.michaldrabik.data_local.database.dao.TraktSyncQueueDao
import com.michaldrabik.data_local.database.dao.TranslationsMoviesSyncLogDao
import com.michaldrabik.data_local.database.dao.TranslationsSyncLogDao
import com.michaldrabik.data_local.database.dao.UserDao
import com.michaldrabik.data_local.database.dao.WatchlistMoviesDao
import com.michaldrabik.data_local.database.dao.WatchlistShowsDao
import com.michaldrabik.data_local.database.migrations.DATABASE_VERSION
import com.michaldrabik.data_local.database.model.ArchiveMovie
import com.michaldrabik.data_local.database.model.ArchiveShow
import com.michaldrabik.data_local.database.model.CustomImage
import com.michaldrabik.data_local.database.model.CustomList
import com.michaldrabik.data_local.database.model.CustomListItem
import com.michaldrabik.data_local.database.model.DiscoverMovie
import com.michaldrabik.data_local.database.model.DiscoverShow
import com.michaldrabik.data_local.database.model.Episode
import com.michaldrabik.data_local.database.model.EpisodeTranslation
import com.michaldrabik.data_local.database.model.EpisodesSyncLog
import com.michaldrabik.data_local.database.model.Movie
import com.michaldrabik.data_local.database.model.MovieImage
import com.michaldrabik.data_local.database.model.MovieRatings
import com.michaldrabik.data_local.database.model.MovieStreaming
import com.michaldrabik.data_local.database.model.MovieTranslation
import com.michaldrabik.data_local.database.model.MoviesSyncLog
import com.michaldrabik.data_local.database.model.MyMovie
import com.michaldrabik.data_local.database.model.MyShow
import com.michaldrabik.data_local.database.model.News
import com.michaldrabik.data_local.database.model.Person
import com.michaldrabik.data_local.database.model.PersonCredits
import com.michaldrabik.data_local.database.model.PersonImage
import com.michaldrabik.data_local.database.model.PersonShowMovie
import com.michaldrabik.data_local.database.model.Rating
import com.michaldrabik.data_local.database.model.RecentSearch
import com.michaldrabik.data_local.database.model.RelatedMovie
import com.michaldrabik.data_local.database.model.RelatedShow
import com.michaldrabik.data_local.database.model.Season
import com.michaldrabik.data_local.database.model.Settings
import com.michaldrabik.data_local.database.model.Show
import com.michaldrabik.data_local.database.model.ShowImage
import com.michaldrabik.data_local.database.model.ShowRatings
import com.michaldrabik.data_local.database.model.ShowStreaming
import com.michaldrabik.data_local.database.model.ShowTranslation
import com.michaldrabik.data_local.database.model.TraktSyncLog
import com.michaldrabik.data_local.database.model.TraktSyncQueue
import com.michaldrabik.data_local.database.model.TranslationsMoviesSyncLog
import com.michaldrabik.data_local.database.model.TranslationsSyncLog
import com.michaldrabik.data_local.database.model.User
import com.michaldrabik.data_local.database.model.WatchlistMovie
import com.michaldrabik.data_local.database.model.WatchlistShow

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
    ArchiveMovie::class,
    RelatedShow::class,
    RelatedMovie::class,
    ShowImage::class,
    MovieImage::class,
    User::class,
    Season::class,
    Person::class,
    PersonShowMovie::class,
    PersonCredits::class,
    PersonImage::class,
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
    EpisodeTranslation::class,
    CustomImage::class,
    CustomList::class,
    CustomListItem::class,
    News::class,
    Rating::class,
    ShowRatings::class,
    MovieRatings::class,
    ShowStreaming::class,
    MovieStreaming::class,
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

  abstract fun archiveMoviesDao(): ArchiveMoviesDao

  abstract fun relatedShowsDao(): RelatedShowsDao

  abstract fun relatedMoviesDao(): RelatedMoviesDao

  abstract fun showImagesDao(): ShowImagesDao

  abstract fun movieImagesDao(): MovieImagesDao

  abstract fun customImagesDao(): CustomImagesDao

  abstract fun userDao(): UserDao

  abstract fun recentSearchDao(): RecentSearchDao

  abstract fun episodesDao(): EpisodesDao

  abstract fun seasonsDao(): SeasonsDao

  abstract fun peopleDao(): PeopleDao

  abstract fun peopleShowsMoviesDao(): PeopleShowsMoviesDao

  abstract fun peopleCreditsDao(): PeopleCreditsDao

  abstract fun peopleImagesDao(): PeopleImagesDao

  abstract fun settingsDao(): SettingsDao

  abstract fun traktSyncLogDao(): TraktSyncLogDao

  abstract fun moviesSyncLogDao(): MoviesSyncLogDao

  abstract fun episodesSyncLogDao(): EpisodesSyncLogDao

  abstract fun translationsSyncLogDao(): TranslationsSyncLogDao

  abstract fun translationsMoviesSyncLogDao(): TranslationsMoviesSyncLogDao

  abstract fun traktSyncQueueDao(): TraktSyncQueueDao

  abstract fun showTranslationsDao(): ShowTranslationsDao

  abstract fun movieTranslationsDao(): MovieTranslationsDao

  abstract fun ratingsDao(): RatingsDao

  abstract fun showRatingsDao(): ShowRatingsDao

  abstract fun movieRatingsDao(): MovieRatingsDao

  abstract fun showStreamingsDao(): ShowStreamingsDao

  abstract fun movieStreamingsDao(): MovieStreamingsDao

  abstract fun episodeTranslationsDao(): EpisodeTranslationsDao

  abstract fun customListsDao(): CustomListsDao

  abstract fun customListsItemsDao(): CustomListsItemsDao

  abstract fun newsDao(): NewsDao
}
