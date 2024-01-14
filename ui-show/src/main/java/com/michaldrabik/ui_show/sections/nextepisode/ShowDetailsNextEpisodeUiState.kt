package com.michaldrabik.ui_show.sections.nextepisode

import com.michaldrabik.ui_model.SpoilersSettings
import com.michaldrabik.ui_show.sections.nextepisode.helpers.NextEpisodeBundle

data class ShowDetailsNextEpisodeUiState(
  val nextEpisode: NextEpisodeBundle? = null,
  val spoilersSettings: SpoilersSettings? = null
)
