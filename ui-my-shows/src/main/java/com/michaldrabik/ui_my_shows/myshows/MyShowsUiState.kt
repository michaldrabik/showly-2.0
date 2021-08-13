package com.michaldrabik.ui_my_shows.myshows

import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_my_shows.myshows.recycler.MyShowsItem

data class MyShowsUiState(
  val items: List<MyShowsItem>? = null,
  val notifyListsUpdate: ActionEvent<Boolean>? = null,
)
