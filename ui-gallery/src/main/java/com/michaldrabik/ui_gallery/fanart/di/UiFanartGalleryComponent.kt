package com.michaldrabik.ui_gallery.fanart.di

import com.michaldrabik.ui_gallery.fanart.ArtGalleryFragment
import dagger.Subcomponent

@Subcomponent
interface UiFanartGalleryComponent {

  @Subcomponent.Factory
  interface Factory {
    fun create(): UiFanartGalleryComponent
  }

  fun inject(fragment: ArtGalleryFragment)
}
