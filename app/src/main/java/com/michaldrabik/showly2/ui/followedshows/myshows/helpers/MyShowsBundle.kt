package com.michaldrabik.showly2.ui.followedshows.myshows.helpers

import com.michaldrabik.showly2.ui.followedshows.myshows.recycler.MyShowsListItem
import com.michaldrabik.ui_model.MyShowsSection
import com.michaldrabik.ui_model.SortOrder

data class MyShowsBundle(
  val items: List<MyShowsListItem>,
  val section: MyShowsSection,
  val sortOrder: SortOrder?,
  val isCollapsed: Boolean?,
  val isVisible: Boolean?
)
