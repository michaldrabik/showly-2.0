package com.michaldrabik.ui_my_movies.mymovies

import com.michaldrabik.ui_my_movies.mymovies.recycler.MyMoviesItem

data class MyMoviesUiState(
  val items: List<MyMoviesItem>? = null,
)
