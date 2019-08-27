package com.michaldrabik.showly2.ui.discover

import com.michaldrabik.network.trakt.model.Show

data class DiscoverUiModel(
  val trendingShows: List<Show>? = null
)