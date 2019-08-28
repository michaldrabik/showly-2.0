package com.michaldrabik.showly2.ui.discover

import com.michaldrabik.network.trakt.model.Ids
import com.michaldrabik.showly2.ui.discover.recycler.DiscoverListItem

data class DiscoverUiModel(
  val trendingShows: List<DiscoverListItem>? = null,
  val showLoading: Boolean? = null,
  val missingImage: Pair<Ids, String>? = null
)