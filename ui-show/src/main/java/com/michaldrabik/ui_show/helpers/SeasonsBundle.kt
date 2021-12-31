package com.michaldrabik.ui_show.helpers

import com.michaldrabik.ui_show.seasons.SeasonListItem

data class SeasonsBundle(
  val seasons: List<SeasonListItem>,
  val isLocal: Boolean
)
