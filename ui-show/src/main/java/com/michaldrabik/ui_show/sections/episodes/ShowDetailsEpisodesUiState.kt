package com.michaldrabik.ui_show.sections.episodes

import com.michaldrabik.ui_model.Ratings
import com.michaldrabik.ui_model.Show

data class ShowDetailsEpisodesUiState(
  val show: Show? = null,
  val ratings: Ratings? = null
)
