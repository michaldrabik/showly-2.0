package com.michaldrabik.showly2.ui.show.seasons

import com.michaldrabik.showly2.model.Season
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.ui.show.seasons.episodes.EpisodeListItem

data class SeasonListItem(
  val season: Season,
  val episodes: List<EpisodeListItem>,
  val isWatched: Boolean,
  val show: Show
) {

  val id = season.traktId

}