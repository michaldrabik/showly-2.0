package com.michaldrabik.ui_show.sections.episodes

import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_show.sections.episodes.recycler.EpisodeListItem
import com.michaldrabik.ui_show.sections.seasons.recycler.SeasonListItem

data class ShowDetailsEpisodesUiState(
  val show: Show? = null,
  val season: SeasonListItem? = null,
  val episodes: List<EpisodeListItem>? = null
)
