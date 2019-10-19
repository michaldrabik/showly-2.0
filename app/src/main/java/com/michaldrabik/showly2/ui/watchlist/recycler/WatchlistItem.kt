package com.michaldrabik.showly2.ui.watchlist.recycler

import com.michaldrabik.showly2.model.Episode
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.Season
import com.michaldrabik.showly2.model.SeasonBundle
import com.michaldrabik.showly2.model.Show

data class WatchlistItem(
  val show: Show,
  val season: Season,
  val episode: Episode,
  val image: Image,
  val episodesCount: Int,
  val watchedEpisodesCount: Int
)