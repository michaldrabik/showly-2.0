package com.michaldrabik.ui_show.sections.seasons.helpers

import com.michaldrabik.ui_show.sections.seasons.recycler.SeasonListItem

data class SeasonsBundle(
  val seasons: List<SeasonListItem>?,
  val isLocal: Boolean
)
