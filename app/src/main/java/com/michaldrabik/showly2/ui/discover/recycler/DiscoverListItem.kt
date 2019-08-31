package com.michaldrabik.showly2.ui.discover.recycler

import com.michaldrabik.showly2.model.ImageUrl
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.ui.common.ImageType

data class DiscoverListItem(
  val show: Show,
  var imageUrl: ImageUrl,
  var type: ImageType = ImageType.POSTER,
  var isLoading: Boolean = false
)