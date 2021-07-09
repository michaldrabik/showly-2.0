package com.michaldrabik.ui_progress.calendar.helpers.filters

import com.michaldrabik.common.extensions.toLocalZone
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit.DAYS
import javax.inject.Inject
import javax.inject.Singleton
import com.michaldrabik.data_local.database.model.Episode as EpisodeDb

@Singleton
class CalendarRecentsFilter @Inject constructor() : CalendarFilter {

  override fun filter(now: ZonedDateTime, episode: EpisodeDb): Boolean {
    val dateDays = episode.firstAired?.toLocalZone()?.truncatedTo(DAYS)
    val isHistory = dateDays?.isBefore(now.truncatedTo(DAYS)) == true
    val isLast3Months = dateDays?.isAfter(now.truncatedTo(DAYS).minusMonths(3)) == true
    return episode.seasonNumber != 0 && isHistory && isLast3Months
  }
}
