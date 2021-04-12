package com.michaldrabik.ui_lists.manage.di

import com.michaldrabik.ui_lists.manage.ManageListsBottomSheet
import dagger.Subcomponent

@Subcomponent
interface UiManageListsComponent {

  @Subcomponent.Factory
  interface Factory {
    fun create(): UiManageListsComponent
  }

  fun inject(fragment: ManageListsBottomSheet)
}
