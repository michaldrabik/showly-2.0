package com.michaldrabik.showly2.ui.show.seasons.episodes.details

import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.ui.common.UiModel

data class EpisodeDetailsUiModel(
  val image: Image? = null,
  val imageLoading: Boolean? = null
) : UiModel()