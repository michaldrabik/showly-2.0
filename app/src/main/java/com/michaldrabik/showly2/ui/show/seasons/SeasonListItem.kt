package com.michaldrabik.showly2.ui.show.seasons

import com.michaldrabik.showly2.ui.show.seasons.episodes.EpisodeListItem
import com.michaldrabik.ui_model.Season

data class SeasonListItem(
  val season: Season,
  val episodes: List<EpisodeListItem>,
  val isWatched: Boolean
) {

  val id = season.ids.trakt.id
}
