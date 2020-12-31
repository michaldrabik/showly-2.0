package com.michaldrabik.ui_show.seasons

import com.michaldrabik.ui_model.Season
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_show.episodes.EpisodeListItem

data class SeasonListItem(
  val show: Show,
  val season: Season,
  val episodes: List<EpisodeListItem>,
  val isWatched: Boolean
) {

  val id = season.ids.trakt.id
}
