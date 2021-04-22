package com.michaldrabik.showly2.ui

import com.michaldrabik.showly2.appComponent
import com.michaldrabik.ui_comments.post.di.UiPostCommentComponentProvider
import com.michaldrabik.ui_discover.di.UiDiscoverComponentProvider
import com.michaldrabik.ui_discover_movies.di.UiDiscoverMoviesComponentProvider
import com.michaldrabik.ui_episodes.details.di.UiEpisodeDetailsComponentProvider
import com.michaldrabik.ui_gallery.custom.di.UiCustomImagesComponentProvider
import com.michaldrabik.ui_gallery.fanart.di.UiArtGalleryComponentProvider
import com.michaldrabik.ui_lists.create.di.UiCreateListComponentProvider
import com.michaldrabik.ui_lists.details.di.UiListDetailsComponentProvider
import com.michaldrabik.ui_lists.lists.di.UiListsComponentProvider
import com.michaldrabik.ui_lists.manage.di.UiManageListsComponentProvider
import com.michaldrabik.ui_movie.di.UiMovieDetailsComponentProvider
import com.michaldrabik.ui_my_movies.di.UiMyMoviesComponentProvider
import com.michaldrabik.ui_my_shows.di.UiMyShowsComponentProvider
import com.michaldrabik.ui_news.di.UiNewsComponentProvider
import com.michaldrabik.ui_premium.di.UiPremiumComponentProvider
import com.michaldrabik.ui_progress.di.UiProgressComponentProvider
import com.michaldrabik.ui_progress_movies.di.UiProgressMoviesComponentProvider
import com.michaldrabik.ui_search.di.UiSearchComponentProvider
import com.michaldrabik.ui_settings.di.UiSettingsComponentProvider
import com.michaldrabik.ui_show.di.UiShowDetailsComponentProvider
import com.michaldrabik.ui_statistics.di.UiStatisticsComponentProvider
import com.michaldrabik.ui_statistics_movies.di.UiStatisticsMoviesComponentProvider
import com.michaldrabik.ui_trakt_sync.di.UiTraktSyncComponentProvider

abstract class DiActivity :
  BaseActivity(),
  UiTraktSyncComponentProvider,
  UiStatisticsComponentProvider,
  UiStatisticsMoviesComponentProvider,
  UiDiscoverComponentProvider,
  UiDiscoverMoviesComponentProvider,
  UiShowDetailsComponentProvider,
  UiMovieDetailsComponentProvider,
  UiArtGalleryComponentProvider,
  UiEpisodeDetailsComponentProvider,
  UiCustomImagesComponentProvider,
  UiMyShowsComponentProvider,
  UiMyMoviesComponentProvider,
  UiListsComponentProvider,
  UiListDetailsComponentProvider,
  UiCreateListComponentProvider,
  UiManageListsComponentProvider,
  UiProgressComponentProvider,
  UiProgressMoviesComponentProvider,
  UiSearchComponentProvider,
  UiSettingsComponentProvider,
  UiPostCommentComponentProvider,
  UiNewsComponentProvider,
  UiPremiumComponentProvider {

  override fun provideDiscoverComponent() = appComponent().uiDiscoverComponent().create()
  override fun provideDiscoverMoviesComponent() = appComponent().uiDiscoverMoviesComponent().create()
  override fun provideEpisodeDetailsComponent() = appComponent().uiEpisodeDetailsComponent().create()
  override fun provideCustomImagesComponent() = appComponent().uiCustomImagesComponent().create()
  override fun provideArtGalleryComponent() = appComponent().uiShowGalleryComponent().create()
  override fun provideMyShowsComponent() = appComponent().uiMyShowsComponent().create()
  override fun provideMyMoviesComponent() = appComponent().uiMyMoviesComponent().create()
  override fun provideSearchComponent() = appComponent().uiSearchComponent().create()
  override fun provideSettingsComponent() = appComponent().uiSettingsComponent().create()
  override fun provideShowDetailsComponent() = appComponent().uiShowDetailsComponent().create()
  override fun provideMovieDetailsComponent() = appComponent().uiMovieDetailsComponent().create()
  override fun provideStatisticsComponent() = appComponent().uiStatisticsComponent().create()
  override fun provideStatisticsMoviesComponent() = appComponent().uiStatisticsMoviesComponent().create()
  override fun provideTraktSyncComponent() = appComponent().uiTraktSyncComponent().create()
  override fun provideProgressComponent() = appComponent().uiProgressComponent().create()
  override fun provideProgressMoviesComponent() = appComponent().uiProgressMoviesComponent().create()
  override fun providePremiumComponent() = appComponent().uiPremiumComponent().create()
  override fun providePostCommentComponent() = appComponent().uiPostCommentComponent().create()
  override fun provideListsComponent() = appComponent().uiListsComponent().create()
  override fun provideListDetailsComponent() = appComponent().uiListDetailsComponent().create()
  override fun provideCreateListComponent() = appComponent().uiCreateListComponent().create()
  override fun provideManageListsComponent() = appComponent().uiManageListsComponent().create()
  override fun provideNewsComponent() = appComponent().uiNewsComponent().create()
}
