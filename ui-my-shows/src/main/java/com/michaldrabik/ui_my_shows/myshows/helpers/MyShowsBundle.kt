package com.michaldrabik.ui_my_shows.myshows.helpers

import com.michaldrabik.ui_model.MyShowsSection
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_my_shows.myshows.recycler.MyShowsListItem

data class MyShowsBundle(
  val items: List<MyShowsListItem>,
  val section: MyShowsSection,
  val sortOrder: SortOrder?,
  val isCollapsed: Boolean?,
  val isVisible: Boolean?
)
