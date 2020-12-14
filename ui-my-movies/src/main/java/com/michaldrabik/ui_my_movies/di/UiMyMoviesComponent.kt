package com.michaldrabik.ui_my_movies.di

import com.michaldrabik.ui_my_movies.main.FollowedMoviesFragment
import com.michaldrabik.ui_my_movies.mymovies.MyMoviesFragment
import com.michaldrabik.ui_my_movies.watchlist.WatchlistFragment
import dagger.Subcomponent

@Subcomponent
interface UiMyMoviesComponent {

  @Subcomponent.Factory
  interface Factory {
    fun create(): UiMyMoviesComponent
  }

  fun inject(fragment: FollowedMoviesFragment)

  fun inject(fragment: MyMoviesFragment)

  fun inject(fragment: WatchlistFragment)
}
