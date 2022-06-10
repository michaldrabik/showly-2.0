package com.michaldrabik.ui_show.sections.seasons.recycler

import com.michaldrabik.ui_model.RatingState
import com.michaldrabik.ui_model.Season
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_show.episodes.recycler.EpisodeListItem

data class SeasonListItem(
  val show: Show,
  val season: Season,
  val episodes: List<EpisodeListItem>,
  val isWatched: Boolean,
  val userRating: RatingState,
  val updatedAt: Long,
) {

  val id = season.ids.trakt.id
}
