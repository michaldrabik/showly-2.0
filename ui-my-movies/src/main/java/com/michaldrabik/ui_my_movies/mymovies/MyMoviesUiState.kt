package com.michaldrabik.ui_my_movies.mymovies

import com.michaldrabik.ui_base.common.ListViewMode
import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_my_movies.mymovies.recycler.MyMoviesItem

data class MyMoviesUiState(
  val items: List<MyMoviesItem>? = null,
  val showEmptyView: Boolean = false,
  val viewMode: ListViewMode = ListViewMode.LIST_NORMAL,
  val resetScroll: Event<Boolean>? = null,
)
