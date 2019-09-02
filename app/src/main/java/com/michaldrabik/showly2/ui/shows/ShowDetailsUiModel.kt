package com.michaldrabik.showly2.ui.shows

import com.michaldrabik.showly2.model.Episode
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.Show

data class ShowDetailsUiModel(
  val show: Show? = null,
  val image: Image? = null,
  val imageLoading: Boolean? = null,
  val nextEpisode: Episode? = null
)