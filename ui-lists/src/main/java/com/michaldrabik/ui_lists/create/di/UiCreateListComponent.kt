package com.michaldrabik.ui_lists.create.di

import com.michaldrabik.ui_lists.create.CreateListBottomSheet
import dagger.Subcomponent

@Subcomponent
interface UiCreateListComponent {

  @Subcomponent.Factory
  interface Factory {
    fun create(): UiCreateListComponent
  }

  fun inject(fragment: CreateListBottomSheet)
}
