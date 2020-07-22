package com.michaldrabik.showly2.model

import com.michaldrabik.showly2.model.ShowStatus.CANCELED
import com.michaldrabik.showly2.model.ShowStatus.IN_PRODUCTION
import com.michaldrabik.showly2.model.ShowStatus.PLANNED
import com.michaldrabik.showly2.model.ShowStatus.RETURNING

enum class MyShowsSection(
  val statuses: List<ShowStatus> = emptyList(),
  val displayString: String = ""
) {
  RECENTS(
    displayString = "Recently Added"
  ),
  WATCHING(
    statuses = listOf(RETURNING),
    displayString = "Watching"
  ),
  FINISHED(
    statuses = listOf(CANCELED, ShowStatus.ENDED),
    displayString = "Finished"
  ),
  UPCOMING(
    statuses = listOf(IN_PRODUCTION, PLANNED),
    displayString = "Upcoming & Returning"
  ),
  ALL(
    displayString = "All"
  )
}
