package com.michaldrabik.ui_progress.calendar.helpers.filters

import com.michaldrabik.data_local.database.model.Episode
import org.threeten.bp.ZonedDateTime

interface CalendarFilter {
  fun filter(now: ZonedDateTime, episode: Episode): Boolean
}
