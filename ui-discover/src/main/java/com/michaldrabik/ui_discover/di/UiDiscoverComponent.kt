package com.michaldrabik.ui_discover.di

import com.michaldrabik.ui_discover.DiscoverFragment
import dagger.Subcomponent

@Subcomponent
interface UiDiscoverComponent {

  @Subcomponent.Factory
  interface Factory {
    fun create(): UiDiscoverComponent
  }

  fun inject(fragment: DiscoverFragment)
}
