package com.michaldrabik.ui_lists.lists.di

import com.michaldrabik.ui_lists.lists.ListsFragment
import dagger.Subcomponent

@Subcomponent
interface UiListsComponent {

  @Subcomponent.Factory
  interface Factory {
    fun create(): UiListsComponent
  }

  fun inject(fragment: ListsFragment)
}
