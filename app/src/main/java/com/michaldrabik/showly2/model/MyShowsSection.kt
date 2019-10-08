package com.michaldrabik.showly2.model

import com.michaldrabik.showly2.model.ShowStatus.*

enum class MyShowsSection(val statuses: List<ShowStatus>) {
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