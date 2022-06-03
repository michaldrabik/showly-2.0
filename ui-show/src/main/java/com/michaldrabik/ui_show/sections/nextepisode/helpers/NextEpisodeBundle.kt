package com.michaldrabik.ui_show.sections.nextepisode.helpers

import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.Show
import java.time.format.DateTimeFormatter

data class NextEpisodeBundle(
  val nextEpisode: Pair<Show, Episode>,
  val dateFormat: DateTimeFormatter? = null,
)
