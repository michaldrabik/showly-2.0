package com.michaldrabik.ui_show.gallery.di

import com.michaldrabik.ui_show.gallery.FanartGalleryFragment
import dagger.Subcomponent

@Subcomponent
interface UiFanartGalleryComponent {

  @Subcomponent.Factory
  interface Factory {
    fun create(): UiFanartGalleryComponent
  }

  fun inject(fragment: FanartGalleryFragment)
}
