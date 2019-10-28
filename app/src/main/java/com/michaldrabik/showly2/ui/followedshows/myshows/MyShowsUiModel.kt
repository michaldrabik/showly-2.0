package com.michaldrabik.showly2.ui.followedshows.myshows

import com.michaldrabik.showly2.model.MyShowsSection
import com.michaldrabik.showly2.ui.common.UiModel
import com.michaldrabik.showly2.ui.followedshows.myshows.helpers.MyShowsBundle
import com.michaldrabik.showly2.ui.followedshows.myshows.recycler.MyShowsListItem

data class MyShowsUiModel(
  val recentShows: List<MyShowsListItem>? = null,
  val runningShows: MyShowsBundle? = null,
  val endedShows: MyShowsBundle? = null,
  val incomingShows: MyShowsBundle? = null,
  val updateListItem: MyShowsListItem? = null,
  val updateSectionListItems: Pair<MyShowsSection, List<MyShowsListItem>>? = null,
  val sectionsPositions: Map<MyShowsSection, Pair<Int, Int>>? = null
) : UiModel