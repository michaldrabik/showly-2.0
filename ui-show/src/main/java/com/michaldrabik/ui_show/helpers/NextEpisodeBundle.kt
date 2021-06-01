package com.michaldrabik.ui_show.helpers

import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.Show
import org.threeten.bp.format.DateTimeFormatter

data class NextEpisodeBundle(
  val nextEpisode: Pair<Show, Episode>,
  val dateFormat: DateTimeFormatter? = null,
)
