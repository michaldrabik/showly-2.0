package com.michaldrabik.ui_lists.details.di

import com.michaldrabik.ui_lists.details.ListDetailsFragment
import dagger.Subcomponent

@Subcomponent
interface UiListDetailsComponent {

  @Subcomponent.Factory
  interface Factory {
    fun create(): UiListDetailsComponent
  }

  fun inject(fragment: ListDetailsFragment)
}
