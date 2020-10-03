package com.michaldrabik.ui_trakt_sync.di

import com.michaldrabik.showly2.ui.show.gallery.FanartGalleryFragment
import dagger.Subcomponent

@Subcomponent
interface UiFanartGalleryComponent {

  @Subcomponent.Factory
  interface Factory {
    fun create(): UiFanartGalleryComponent
  }

  fun inject(fragment: FanartGalleryFragment)
}
