package com.michaldrabik.showly2.ui.watchlist.recycler

import com.michaldrabik.showly2.model.Episode
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.Show

data class WatchlistItem(
  val show: Show,
  val episode: Episode,
  val image: Image
)