package com.michaldrabik.ui_statistics.di

import com.michaldrabik.ui_search.SearchFragment
import dagger.Subcomponent

@Subcomponent
interface UiSearchComponent {

  @Subcomponent.Factory
  interface Factory {
    fun create(): UiSearchComponent
  }

  fun inject(fragment: SearchFragment)
}
