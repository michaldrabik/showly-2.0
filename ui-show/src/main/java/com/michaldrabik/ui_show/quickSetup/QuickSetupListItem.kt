package com.michaldrabik.ui_show.quickSetup

import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.Season

data class QuickSetupListItem(
  val episode: Episode,
  val season: Season,
  val isHeader: Boolean = false,
  val isChecked: Boolean = false
)
