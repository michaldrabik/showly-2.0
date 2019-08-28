package com.michaldrabik.showly2.ui.discover.recycler

import com.michaldrabik.network.trakt.model.Show
import com.michaldrabik.showly2.model.ImageUrl

data class DiscoverListItem(
  val show: Show,
  var imageUrl: ImageUrl,
  var type: Type = Type.POSTER,
  var isLoading: Boolean = false
) {

  enum class Type {
    POSTER,
    FANART
  }
}