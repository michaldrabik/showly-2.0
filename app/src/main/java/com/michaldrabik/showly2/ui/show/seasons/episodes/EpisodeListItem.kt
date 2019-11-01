package com.michaldrabik.showly2.ui.show.seasons.episodes

import com.michaldrabik.showly2.model.Episode
import com.michaldrabik.showly2.model.Season

data class EpisodeListItem(
  val episode: Episode,
  val season: Season,
  val isWatched: Boolean
) {

  val id = episode.ids.trakt.id

}