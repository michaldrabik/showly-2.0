package com.michaldrabik.ui_trakt_sync.di

import com.michaldrabik.ui_trakt_sync.TraktSyncFragment
import dagger.Subcomponent

@Subcomponent
interface UiTraktSyncComponent {

  @Subcomponent.Factory
  interface Factory {
    fun create(): UiTraktSyncComponent
  }

  fun inject(fragment: TraktSyncFragment)
}
