package com.michaldrabik.ui_model

import com.michaldrabik.ui_model.ShowStatus.CANCELED
import com.michaldrabik.ui_model.ShowStatus.ENDED
import com.michaldrabik.ui_model.ShowStatus.IN_PRODUCTION
import com.michaldrabik.ui_model.ShowStatus.PLANNED
import com.michaldrabik.ui_model.ShowStatus.RETURNING

enum class MyShowsSection(
  val displayString: String,
  val statuses: List<ShowStatus> = emptyList()
) {
  RECENTS(
    displayString = "Recently Added"
  ),
  WATCHING(
    statuses = listOf(RETURNING),
    displayString = "Watching"
  ),
  FINISHED(
    statuses = listOf(CANCELED, ENDED),
    displayString = "Finished"
  ),
  UPCOMING(
    statuses = listOf(IN_PRODUCTION, PLANNED, ShowStatus.UPCOMING),
    displayString = "Returning & Upcoming"
  ),
  ALL(
    displayString = "All"
  )
}
