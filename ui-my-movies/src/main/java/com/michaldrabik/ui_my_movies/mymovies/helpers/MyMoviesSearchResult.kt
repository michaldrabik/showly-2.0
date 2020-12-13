package com.michaldrabik.ui_my_movies.mymovies.helpers

import com.michaldrabik.ui_my_movies.mymovies.recycler.MyMoviesItem

data class MyMoviesSearchResult(
  val items: List<MyMoviesItem>,
  val type: ResultType
)

enum class ResultType {
  EMPTY,
  RESULTS,
  NO_RESULTS
}
