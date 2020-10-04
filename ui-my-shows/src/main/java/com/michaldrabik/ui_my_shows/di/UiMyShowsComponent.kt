package com.michaldrabik.ui_my_shows.di

import com.michaldrabik.ui_my_shows.archive.ArchiveFragment
import com.michaldrabik.ui_my_shows.main.FollowedShowsFragment
import com.michaldrabik.ui_my_shows.myshows.MyShowsFragment
import com.michaldrabik.ui_my_shows.seelater.SeeLaterFragment
import dagger.Subcomponent

@Subcomponent
interface UiMyShowsComponent {

  @Subcomponent.Factory
  interface Factory {
    fun create(): UiMyShowsComponent
  }

  fun inject(fragment: FollowedShowsFragment)

  fun inject(fragment: MyShowsFragment)

  fun inject(fragment: SeeLaterFragment)

  fun inject(fragment: ArchiveFragment)
}
