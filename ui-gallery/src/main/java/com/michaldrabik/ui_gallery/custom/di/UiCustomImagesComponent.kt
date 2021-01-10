package com.michaldrabik.ui_gallery.custom.di

import com.michaldrabik.ui_gallery.custom.CustomImagesBottomSheet
import dagger.Subcomponent

@Subcomponent
interface UiCustomImagesComponent {

  @Subcomponent.Factory
  interface Factory {
    fun create(): UiCustomImagesComponent
  }

  fun inject(fragment: CustomImagesBottomSheet)
}
