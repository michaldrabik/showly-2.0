package com.michaldrabik.ui_lists.di

import com.michaldrabik.ui_lists.MyListsFragment
import dagger.Subcomponent

@Subcomponent
interface UiMyListsComponent {

  @Subcomponent.Factory
  interface Factory {
    fun create(): UiMyListsComponent
  }

  fun inject(fragment: MyListsFragment)
}
