package com.michaldrabik.ui_my_movies.mymovies

import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_my_movies.mymovies.recycler.MyMoviesItem

data class MyMoviesUiState(
  val items: List<MyMoviesItem>? = null,
  val resetScroll: Event<Boolean>? = null,
)
