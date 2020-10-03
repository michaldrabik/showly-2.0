package com.michaldrabik.showly2.di.component

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.network.di.CloudMarker
import com.michaldrabik.showly2.di.module.PreferencesModule
import com.michaldrabik.showly2.di.module.SubcomponentsModule
import com.michaldrabik.showly2.di.module.ViewModelsModule
import com.michaldrabik.showly2.ui.main.MainActivity
import com.michaldrabik.storage.di.StorageMarker
import com.michaldrabik.ui_base.di.UiBaseComponent
import com.michaldrabik.ui_settings.di.UiSettingsComponent
import com.michaldrabik.ui_show.di.UiShowDetailsComponent
import com.michaldrabik.ui_statistics.di.UiSearchComponent
import com.michaldrabik.ui_statistics.di.UiStatisticsComponent
import com.michaldrabik.ui_trakt_sync.di.UiTraktSyncComponent
import dagger.Component

@AppScope
@Component(
  dependencies = [
    CloudMarker::class,
    StorageMarker::class
  ],
  modules = [
    ViewModelsModule::class,
    PreferencesModule::class,
    SubcomponentsModule::class
  ]
)
interface AppComponent {

  fun fragmentComponent(): FragmentComponent.Factory

  fun serviceComponent(): ServiceComponent.Factory

  fun uiBaseComponent(): UiBaseComponent.Factory

  fun uiSettingsComponent(): UiSettingsComponent.Factory

  fun uiTraktSyncComponent(): UiTraktSyncComponent.Factory

  fun uiShowDetailsComponent(): UiShowDetailsComponent.Factory

  fun uiSearchComponent(): UiSearchComponent.Factory

  fun uiStatisticsComponent(): UiStatisticsComponent.Factory

  fun inject(activity: MainActivity)
}
