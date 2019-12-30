package com.michaldrabik.showly2.model

import com.michaldrabik.showly2.model.ShowStatus.CANCELED
import com.michaldrabik.showly2.model.ShowStatus.IN_PRODUCTION
import com.michaldrabik.showly2.model.ShowStatus.PLANNED
import com.michaldrabik.showly2.model.ShowStatus.RETURNING

enum class MyShowsSection(val statuses: List<ShowStatus> = emptyList()) {
  ALL,
  RUNNING(
    listOf(RETURNING)
  ),
  ENDED(
    listOf(CANCELED, ShowStatus.ENDED)
  ),
  COMING_SOON(
    listOf(IN_PRODUCTION, PLANNED)
  )
}
