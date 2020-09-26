package com.michaldrabik.showly2.di.component

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.network.di.CloudMarker
import com.michaldrabik.showly2.di.module.PreferencesModule
import com.michaldrabik.showly2.di.module.SubcomponentsModule
import com.michaldrabik.showly2.di.module.ViewModelsModule
import com.michaldrabik.showly2.ui.main.MainActivity
import com.michaldrabik.storage.di.StorageMarker
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

  fun inject(activity: MainActivity)
}
