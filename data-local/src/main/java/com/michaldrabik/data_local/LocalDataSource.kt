package com.michaldrabik.data_local

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
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides local data sources access points.
 */
// TODO Refactor. Split or remove this wrapper at all. Clients do not need to be exposed to everything.
interface LocalDataSource {
  val archiveMovies: ArchiveMoviesLocalDataSource
  val archiveShows: ArchiveShowsLocalDataSource
  val customImages: CustomImagesLocalDataSource
  val customLists: CustomListsLocalDataSource
  val customListsItems: CustomListsItemsLocalDataSource
  val discoverMovies: DiscoverMoviesLocalDataSource
  val discoverShows: DiscoverShowsLocalDataSource
  val episodes: EpisodesLocalDataSource
  val episodesSyncLog: EpisodesSyncLogLocalDataSource
  val episodesTranslations: EpisodeTranslationsLocalDataSource
  val movieImages: MovieImagesLocalDataSource
  val movieRatings: MovieRatingsLocalDataSource
  val movieStreamings: MovieStreamingsLocalDataSource
  val movieTranslations: MovieTranslationsLocalDataSource
  val movies: MoviesLocalDataSource
  val moviesSyncLog: MoviesSyncLogLocalDataSource
  val myMovies: MyMoviesLocalDataSource
  val myShows: MyShowsLocalDataSource
  val news: NewsLocalDataSource
  val people: PeopleLocalDataSource
  val peopleCredits: PeopleCreditsLocalDataSource
  val peopleImages: PeopleImagesLocalDataSource
  val peopleShowsMovies: PeopleShowsMoviesLocalDataSource
  val ratings: RatingsLocalDataSource
  val recentSearch: RecentSearchLocalDataSource
  val relatedMovies: RelatedMoviesLocalDataSource
  val relatedShows: RelatedShowsLocalDataSource
  val seasons: SeasonsLocalDataSource
  val settings: SettingsLocalDataSource
  val showImages: ShowImagesLocalDataSource
  val showRatings: ShowRatingsLocalDataSource
  val showStreamings: ShowStreamingsLocalDataSource
  val showTranslations: ShowTranslationsLocalDataSource
  val shows: ShowsLocalDataSource
  val traktSyncLog: TraktSyncLogLocalDataSource
  val traktSyncQueue: TraktSyncQueueLocalDataSource
  val translationsMoviesSyncLog: TranslationsMoviesSyncLogLocalDataSource
  val translationsShowsSyncLog: TranslationsShowsSyncLogLocalDataSource
  val user: UserLocalDataSource
  val watchlistMovies: WatchlistMoviesLocalDataSource
  val watchlistShows: WatchlistShowsLocalDataSource
}

@Singleton
internal class MainLocalDataSource @Inject constructor(
  override val archiveMovies: ArchiveMoviesLocalDataSource,
  override val archiveShows: ArchiveShowsLocalDataSource,
  override val customImages: CustomImagesLocalDataSource,
  override val customLists: CustomListsLocalDataSource,
  override val customListsItems: CustomListsItemsLocalDataSource,
  override val discoverMovies: DiscoverMoviesLocalDataSource,
  override val discoverShows: DiscoverShowsLocalDataSource,
  override val episodes: EpisodesLocalDataSource,
  override val episodesSyncLog: EpisodesSyncLogLocalDataSource,
  override val episodesTranslations: EpisodeTranslationsLocalDataSource,
  override val movieImages: MovieImagesLocalDataSource,
  override val movieRatings: MovieRatingsLocalDataSource,
  override val movieStreamings: MovieStreamingsLocalDataSource,
  override val movieTranslations: MovieTranslationsLocalDataSource,
  override val movies: MoviesLocalDataSource,
  override val moviesSyncLog: MoviesSyncLogLocalDataSource,
  override val myMovies: MyMoviesLocalDataSource,
  override val myShows: MyShowsLocalDataSource,
  override val news: NewsLocalDataSource,
  override val people: PeopleLocalDataSource,
  override val peopleCredits: PeopleCreditsLocalDataSource,
  override val peopleImages: PeopleImagesLocalDataSource,
  override val peopleShowsMovies: PeopleShowsMoviesLocalDataSource,
  override val ratings: RatingsLocalDataSource,
  override val recentSearch: RecentSearchLocalDataSource,
  override val relatedMovies: RelatedMoviesLocalDataSource,
  override val relatedShows: RelatedShowsLocalDataSource,
  override val seasons: SeasonsLocalDataSource,
  override val settings: SettingsLocalDataSource,
  override val showImages: ShowImagesLocalDataSource,
  override val showRatings: ShowRatingsLocalDataSource,
  override val showStreamings: ShowStreamingsLocalDataSource,
  override val showTranslations: ShowTranslationsLocalDataSource,
  override val shows: ShowsLocalDataSource,
  override val traktSyncLog: TraktSyncLogLocalDataSource,
  override val traktSyncQueue: TraktSyncQueueLocalDataSource,
  override val translationsMoviesSyncLog: TranslationsMoviesSyncLogLocalDataSource,
  override val translationsShowsSyncLog: TranslationsShowsSyncLogLocalDataSource,
  override val user: UserLocalDataSource,
  override val watchlistMovies: WatchlistMoviesLocalDataSource,
  override val watchlistShows: WatchlistShowsLocalDataSource
) : LocalDataSource
