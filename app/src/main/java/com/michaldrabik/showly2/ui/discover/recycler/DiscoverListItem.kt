package com.michaldrabik.showly2.ui.discover.recycler

import com.michaldrabik.network.trakt.model.Show

data class DiscoverListItem(
  val show: Show,
  var imageUrl: String? = null
)