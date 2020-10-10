package com.michaldrabik.ui_episodes.details.di

import com.michaldrabik.ui_episodes.details.EpisodeDetailsBottomSheet
import dagger.Subcomponent

@Subcomponent
interface UiEpisodeDetailsComponent {

  @Subcomponent.Factory
  interface Factory {
    fun create(): UiEpisodeDetailsComponent
  }

  fun inject(fragment: EpisodeDetailsBottomSheet)
}
