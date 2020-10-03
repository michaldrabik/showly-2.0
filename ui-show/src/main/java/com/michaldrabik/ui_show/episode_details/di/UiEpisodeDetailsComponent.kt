package com.michaldrabik.ui_show.episode_details.di

import com.michaldrabik.ui_show.episode_details.EpisodeDetailsBottomSheet
import dagger.Subcomponent

@Subcomponent
interface UiEpisodeDetailsComponent {

  @Subcomponent.Factory
  interface Factory {
    fun create(): UiEpisodeDetailsComponent
  }

  fun inject(fragment: EpisodeDetailsBottomSheet)
}
