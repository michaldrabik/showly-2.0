package com.michaldrabik.showly2.ui.followedshows.seelater

import com.michaldrabik.showly2.ui.followedshows.seelater.recycler.SeeLaterListItem

data class SeeLaterUiModel(
  val items: List<SeeLaterListItem>? = null,
  val updateItem: SeeLaterListItem? = null
)