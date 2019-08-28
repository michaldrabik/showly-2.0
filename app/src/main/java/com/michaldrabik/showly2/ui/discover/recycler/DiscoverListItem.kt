package com.michaldrabik.showly2.ui.discover.recycler

import com.michaldrabik.network.trakt.model.Show
import com.michaldrabik.showly2.model.ImageUrl

data class DiscoverListItem(
  val show: Show,
  var imageUrl: ImageUrl
)