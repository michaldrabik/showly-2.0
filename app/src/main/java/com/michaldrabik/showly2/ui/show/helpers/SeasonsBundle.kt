package com.michaldrabik.showly2.ui.show.helpers

import com.michaldrabik.showly2.model.Season

data class SeasonsBundle(
  val seasons: List<Season>,
  val isLocal: Boolean
)
