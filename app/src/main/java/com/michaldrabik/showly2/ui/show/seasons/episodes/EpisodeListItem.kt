package com.michaldrabik.showly2.ui.show.seasons.episodes

import com.michaldrabik.showly2.model.Episode

data class EpisodeListItem(
  val episode: Episode,
  val isWatched: Boolean
)