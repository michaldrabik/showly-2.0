package com.michaldrabik.ui_base.di

import com.michaldrabik.ui_base.sync.ShowsMoviesSyncService
import com.michaldrabik.ui_base.trakt.TraktSyncService
import com.michaldrabik.ui_base.trakt.quicksync.QuickSyncService
import dagger.Subcomponent

@Subcomponent
interface UiBaseComponent {

  @Subcomponent.Factory
  interface Factory {
    fun create(): UiBaseComponent
  }

  fun inject(service: TraktSyncService)

  fun inject(service: QuickSyncService)

  fun inject(service: ShowsMoviesSyncService)
}
