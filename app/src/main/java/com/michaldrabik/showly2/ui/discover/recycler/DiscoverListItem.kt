package com.michaldrabik.showly2.ui.discover.recycler

import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.Show

data class DiscoverListItem(
  val show: Show,
  val image: Image,
  var isLoading: Boolean = false
)