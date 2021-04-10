package com.michaldrabik.ui_show.helpers

import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.Show

data class NextEpisodeBundle(
  val nextEpisode: Pair<Show, Episode>,
  val enterTransition: ActionEvent<Boolean> = ActionEvent(true),
)
