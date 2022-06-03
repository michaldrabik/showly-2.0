package com.michaldrabik.ui_show.sections.ratings

import com.michaldrabik.ui_model.Ratings
import com.michaldrabik.ui_model.Show

data class ShowDetailsRatingsUiState(
  val show: Show? = null,
  val ratings: Ratings? = null
)
