package com.michaldrabik.ui_progress.calendar.helpers.filters

import com.michaldrabik.data_local.database.model.Episode
import java.time.ZonedDateTime

interface CalendarFilter {
  fun filter(now: ZonedDateTime, episode: Episode): Boolean
}
