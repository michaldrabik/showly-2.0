package com.michaldrabik.ui_discover_movies.di

import com.michaldrabik.ui_discover_movies.DiscoverMoviesFragment
import dagger.Subcomponent

@Subcomponent
interface UiDiscoverMoviesComponent {

  @Subcomponent.Factory
  interface Factory {
    fun create(): UiDiscoverMoviesComponent
  }

  fun inject(fragment: DiscoverMoviesFragment)
}
