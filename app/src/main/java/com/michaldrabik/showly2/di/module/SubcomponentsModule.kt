package com.michaldrabik.showly2.di.module

import com.michaldrabik.showly2.di.component.ServiceComponent
import com.michaldrabik.ui_base.di.UiBaseComponent
import com.michaldrabik.ui_discover.di.UiDiscoverComponent
import com.michaldrabik.ui_discover_movies.di.UiDiscoverMoviesComponent
import com.michaldrabik.ui_episodes.details.di.UiEpisodeDetailsComponent
import com.michaldrabik.ui_gallery.di.UiFanartGalleryComponent
import com.michaldrabik.ui_movie.di.UiMovieDetailsComponent
import com.michaldrabik.ui_my_movies.di.UiMyMoviesComponent
import com.michaldrabik.ui_my_shows.di.UiMyShowsComponent
import com.michaldrabik.ui_progress.di.UiProgressComponent
import com.michaldrabik.ui_progress_movies.di.UiProgressMoviesComponent
import com.michaldrabik.ui_settings.di.UiSettingsComponent
import com.michaldrabik.ui_show.di.UiShowDetailsComponent
import com.michaldrabik.ui_statistics.di.UiSearchComponent
import com.michaldrabik.ui_statistics.di.UiStatisticsComponent
import com.michaldrabik.ui_statistics_movies.di.UiStatisticsMoviesComponent
import com.michaldrabik.ui_trakt_sync.di.UiTraktSyncComponent
import com.michaldrabik.ui_widgets.di.UiWidgetsComponent
import dagger.Module

@Module(
  subcomponents = [
    UiBaseComponent::class,
    UiDiscoverComponent::class,
    UiDiscoverMoviesComponent::class,
    UiEpisodeDetailsComponent::class,
    UiFanartGalleryComponent::class,
    UiMyShowsComponent::class,
    UiMyMoviesComponent::class,
    UiSearchComponent::class,
    UiSettingsComponent::class,
    UiShowDetailsComponent::class,
    UiMovieDetailsComponent::class,
    UiStatisticsComponent::class,
    UiStatisticsMoviesComponent::class,
    UiTraktSyncComponent::class,
    UiProgressComponent::class,
    UiProgressMoviesComponent::class,
    UiWidgetsComponent::class,
    ServiceComponent::class
  ]
)
class SubcomponentsModule
