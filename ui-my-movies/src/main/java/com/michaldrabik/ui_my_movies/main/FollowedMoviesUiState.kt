package com.michaldrabik.ui_my_movies.main

import com.michaldrabik.ui_my_movies.mymovies.helpers.MyMoviesSearchResult

data class FollowedMoviesUiState(
  val searchResult: MyMoviesSearchResult? = null,
)
