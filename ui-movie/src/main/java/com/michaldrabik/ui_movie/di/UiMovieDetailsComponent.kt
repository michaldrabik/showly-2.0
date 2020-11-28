package com.michaldrabik.ui_movie.di

import com.michaldrabik.ui_movie.MovieDetailsFragment
import dagger.Subcomponent

@Subcomponent
interface UiMovieDetailsComponent {

  @Subcomponent.Factory
  interface Factory {
    fun create(): UiMovieDetailsComponent
  }

  fun inject(fragment: MovieDetailsFragment)
}
