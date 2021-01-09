package com.michaldrabik.ui_gallery.fanart.di

import com.michaldrabik.ui_gallery.fanart.FanartGalleryFragment
import dagger.Subcomponent

@Subcomponent
interface UiFanartGalleryComponent {

  @Subcomponent.Factory
  interface Factory {
    fun create(): UiFanartGalleryComponent
  }

  fun inject(fragment: FanartGalleryFragment)
}
