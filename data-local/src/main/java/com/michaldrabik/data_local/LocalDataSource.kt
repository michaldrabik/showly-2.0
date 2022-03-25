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
import com.michaldrabik.data_local.sources.ShowsLocalDataSource
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides local data sources access points.
 */
interface LocalDataSource {
  val shows: ShowsLocalDataSource
  val movies: MoviesLocalDataSource
  val archiveShows: ArchiveShowsLocalDataSource
  val archiveMovies: ArchiveMoviesLocalDataSource
  val customImages: CustomImagesLocalDataSource
  val customListsItems: CustomListsItemsLocalDataSource
  val customLists: CustomListsLocalDataSource
  val discoverShows: DiscoverShowsLocalDataSource
  val discoverMovies: DiscoverMoviesLocalDataSource
  val episodes: EpisodesLocalDataSource
  val episodesSyncLog: EpisodesSyncLogLocalDataSource
  val episodesTranslations: EpisodeTranslationsLocalDataSource
  val movieImages: MovieImagesLocalDataSource
  val movieRatings: MovieRatingsLocalDataSource
  val moviesSyncLog: MoviesSyncLogLocalDataSource
  val movieStreamings: MovieStreamingsLocalDataSource
  val movieTranslations: MovieTranslationsLocalDataSource
}

@Singleton
internal class MainLocalDataSource @Inject constructor(
  override val shows: ShowsLocalDataSource,
  override val movies: MoviesLocalDataSource,
  override val archiveShows: ArchiveShowsLocalDataSource,
  override val archiveMovies: ArchiveMoviesLocalDataSource,
  override val customImages: CustomImagesLocalDataSource,
  override val customListsItems: CustomListsItemsLocalDataSource,
  override val customLists: CustomListsLocalDataSource,
  override val discoverShows: DiscoverShowsLocalDataSource,
  override val discoverMovies: DiscoverMoviesLocalDataSource,
  override val episodes: EpisodesLocalDataSource,
  override val episodesSyncLog: EpisodesSyncLogLocalDataSource,
  override val episodesTranslations: EpisodeTranslationsLocalDataSource,
  override val movieImages: MovieImagesLocalDataSource,
  override val movieRatings: MovieRatingsLocalDataSource,
  override val moviesSyncLog: MoviesSyncLogLocalDataSource,
  override val movieStreamings: MovieStreamingsLocalDataSource,
  override val movieTranslations: MovieTranslationsLocalDataSource
) : LocalDataSource
