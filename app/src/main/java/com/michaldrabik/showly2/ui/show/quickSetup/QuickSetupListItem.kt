package com.michaldrabik.showly2.ui.show.quickSetup

import com.michaldrabik.showly2.model.Episode
import com.michaldrabik.showly2.model.Season

data class QuickSetupListItem(
  val episode: Episode,
  val season: Season,
  val isHeader: Boolean = false,
  val isChecked: Boolean = false
)
