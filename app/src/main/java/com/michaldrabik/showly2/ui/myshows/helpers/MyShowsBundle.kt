package com.michaldrabik.showly2.ui.myshows.helpers

import com.michaldrabik.showly2.model.MyShowsSection
import com.michaldrabik.showly2.model.SortOrder
import com.michaldrabik.showly2.ui.myshows.recycler.MyShowsListItem

data class MyShowsBundle(
  val items: List<MyShowsListItem>,
  val section: MyShowsSection,
  val sortOrder: SortOrder
)