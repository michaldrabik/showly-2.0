package com.michaldrabik.ui_show.di

import com.michaldrabik.ui_show.ShowDetailsFragment
import dagger.Subcomponent

@Subcomponent
interface UiShowDetailsComponent {

  @Subcomponent.Factory
  interface Factory {
    fun create(): UiShowDetailsComponent
  }

  fun inject(fragment: ShowDetailsFragment)
}
