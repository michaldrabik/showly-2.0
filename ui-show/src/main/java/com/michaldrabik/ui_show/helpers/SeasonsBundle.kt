package com.michaldrabik.ui_show.helpers

import com.michaldrabik.ui_model.Season

data class SeasonsBundle(
  val seasons: List<Season>,
  val isLocal: Boolean
)
