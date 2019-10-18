package com.michaldrabik.showly2.ui.discover

import com.michaldrabik.showly2.ui.discover.recycler.DiscoverListItem

data class DiscoverUiModel(
  val showLoading: Boolean? = null,
  val updateListItem: DiscoverListItem? = null,
  val error: Error? = null
)