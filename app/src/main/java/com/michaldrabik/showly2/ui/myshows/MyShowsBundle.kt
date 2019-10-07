package com.michaldrabik.showly2.ui.myshows

import com.michaldrabik.showly2.model.Show

data class MyShowsBundle(
  val recentsShows: List<Show>,
  val runningShows: List<Show>,
  val endedShows: List<Show>,
  val incomingShows: List<Show>
)