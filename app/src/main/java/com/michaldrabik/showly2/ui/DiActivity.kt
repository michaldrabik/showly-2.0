package com.michaldrabik.showly2.ui

import com.michaldrabik.showly2.appComponent
import com.michaldrabik.ui_discover.di.UiDiscoverComponent
import com.michaldrabik.ui_discover.di.UiDiscoverComponentProvider
import com.michaldrabik.ui_discover_movies.di.UiDiscoverMoviesComponent
import com.michaldrabik.ui_discover_movies.di.UiDiscoverMoviesComponentProvider
import com.michaldrabik.ui_episodes.details.di.UiEpisodeDetailsComponent
import com.michaldrabik.ui_episodes.details.di.UiEpisodeDetailsComponentProvider
import com.michaldrabik.ui_gallery.di.UiFanartGalleryComponent
import com.michaldrabik.ui_gallery.di.UiFanartGalleryComponentProvider
import com.michaldrabik.ui_movie.di.UiMovieDetailsComponent
import com.michaldrabik.ui_movie.di.UiMovieDetailsComponentProvider
import com.michaldrabik.ui_my_movies.di.UiMyMoviesComponent
import com.michaldrabik.ui_my_movies.di.UiMyMoviesComponentProvider
import com.michaldrabik.ui_my_shows.di.UiMyShowsComponent
import com.michaldrabik.ui_my_shows.di.UiMyShowsComponentProvider
import com.michaldrabik.ui_progress.di.UiProgressComponent
import com.michaldrabik.ui_progress.di.UiProgressComponentProvider
import com.michaldrabik.ui_search.di.UiSearchComponentProvider
import com.michaldrabik.ui_settings.di.UiSettingsComponent
import com.michaldrabik.ui_settings.di.UiSettingsComponentProvider
import com.michaldrabik.ui_show.di.UiShowDetailsComponent
import com.michaldrabik.ui_show.di.UiShowDetailsComponentProvider
import com.michaldrabik.ui_statistics.di.UiSearchComponent
import com.michaldrabik.ui_statistics.di.UiStatisticsComponent
import com.michaldrabik.ui_statistics.di.UiStatisticsComponentProvider
import com.michaldrabik.ui_trakt_sync.di.UiTraktSyncComponent
import com.michaldrabik.ui_trakt_sync.di.UiTraktSyncComponentProvider

abstract class DiActivity :
  BaseActivity(),
  UiTraktSyncComponentProvider,
  UiStatisticsComponentProvider,
  UiDiscoverComponentProvider,
  UiDiscoverMoviesComponentProvider,
  UiShowDetailsComponentProvider,
  UiMovieDetailsComponentProvider,
  UiFanartGalleryComponentProvider,
  UiEpisodeDetailsComponentProvider,
  UiMyShowsComponentProvider,
  UiMyMoviesComponentProvider,
  UiProgressComponentProvider,
  UiSearchComponentProvider,
  UiSettingsComponentProvider {

  private lateinit var uiDiscoverComponent: UiDiscoverComponent
  private lateinit var uiDiscoverMoviesComponent: UiDiscoverMoviesComponent
  private lateinit var uiEpisodeDetailsComponent: UiEpisodeDetailsComponent
  private lateinit var uiMyShowsComponent: UiMyShowsComponent
  private lateinit var uiMyMoviesComponent: UiMyMoviesComponent
  private lateinit var uiSearchComponent: UiSearchComponent
  private lateinit var uiSettingsComponent: UiSettingsComponent
  private lateinit var uiShowDetailsComponent: UiShowDetailsComponent
  private lateinit var uiMovieDetailsComponent: UiMovieDetailsComponent
  private lateinit var uiShowGalleryComponent: UiFanartGalleryComponent
  private lateinit var uiStatisticsComponent: UiStatisticsComponent
  private lateinit var uiTraktSyncComponent: UiTraktSyncComponent
  private lateinit var uiProgressComponent: UiProgressComponent

  override fun provideDiscoverComponent() = uiDiscoverComponent
  override fun provideDiscoverMoviesComponent() = uiDiscoverMoviesComponent
  override fun provideEpisodeDetailsComponent() = uiEpisodeDetailsComponent
  override fun provideFanartGalleryComponent() = uiShowGalleryComponent
  override fun provideMyShowsComponent() = uiMyShowsComponent
  override fun provideMyMoviesComponent() = uiMyMoviesComponent
  override fun provideSearchComponent() = uiSearchComponent
  override fun provideSettingsComponent() = uiSettingsComponent
  override fun provideShowDetailsComponent() = uiShowDetailsComponent
  override fun provideMovieDetailsComponent() = uiMovieDetailsComponent
  override fun provideStatisticsComponent() = uiStatisticsComponent
  override fun provideTraktSyncComponent() = uiTraktSyncComponent
  override fun provideProgressComponent() = uiProgressComponent

  protected open fun setupComponents() {
    uiDiscoverComponent = appComponent().uiDiscoverComponent().create()
    uiDiscoverMoviesComponent = appComponent().uiDiscoverMoviesComponent().create()
    uiEpisodeDetailsComponent = appComponent().uiEpisodeDetailsComponent().create()
    uiMyShowsComponent = appComponent().uiMyShowsComponent().create()
    uiMyMoviesComponent = appComponent().uiMyMoviesComponent().create()
    uiSearchComponent = appComponent().uiSearchComponent().create()
    uiSettingsComponent = appComponent().uiSettingsComponent().create()
    uiShowDetailsComponent = appComponent().uiShowDetailsComponent().create()
    uiMovieDetailsComponent = appComponent().uiMovieDetailsComponent().create()
    uiShowGalleryComponent = appComponent().uiShowGalleryComponent().create()
    uiStatisticsComponent = appComponent().uiStatisticsComponent().create()
    uiTraktSyncComponent = appComponent().uiTraktSyncComponent().create()
    uiProgressComponent = appComponent().uiWatchlistComponent().create()
  }
}
