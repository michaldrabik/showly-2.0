package com.michaldrabik.showly2.di.module

import com.michaldrabik.showly2.di.component.FragmentComponent
import com.michaldrabik.showly2.di.component.ServiceComponent
import com.michaldrabik.ui_base.di.UiBaseComponent
import com.michaldrabik.ui_discover.di.UiDiscoverComponent
import com.michaldrabik.ui_my_shows.di.UiMyShowsComponent
import com.michaldrabik.ui_settings.di.UiSettingsComponent
import com.michaldrabik.ui_show.di.UiShowDetailsComponent
import com.michaldrabik.ui_show.episode_details.di.UiEpisodeDetailsComponent
import com.michaldrabik.ui_show.gallery.di.UiFanartGalleryComponent
import com.michaldrabik.ui_statistics.di.UiSearchComponent
import com.michaldrabik.ui_statistics.di.UiStatisticsComponent
import com.michaldrabik.ui_trakt_sync.di.UiTraktSyncComponent
import dagger.Module

@Module(
  subcomponents = [
    UiBaseComponent::class,
    UiDiscoverComponent::class,
    UiEpisodeDetailsComponent::class,
    UiFanartGalleryComponent::class,
    UiMyShowsComponent::class,
    UiSearchComponent::class,
    UiSettingsComponent::class,
    UiShowDetailsComponent::class,
    UiStatisticsComponent::class,
    UiTraktSyncComponent::class,
    FragmentComponent::class,
    ServiceComponent::class
  ]
)
class SubcomponentsModule
