package com.michaldrabik.ui_progress_movies.calendar.helpers.groupers

import com.michaldrabik.common.extensions.toLocalZone
import com.michaldrabik.ui_model.CalendarMode
import com.michaldrabik.ui_progress_movies.R
import com.michaldrabik.ui_progress_movies.calendar.recycler.CalendarMovieListItem
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarRecentsGrouper @Inject constructor() : CalendarGrouper {

  override fun groupByTime(
    nowUtc: ZonedDateTime,
    items: List<CalendarMovieListItem.MovieItem>
  ): List<CalendarMovieListItem> {
    val now = nowUtc.toLocalZone().toLocalDate()

    val yesterdayItems = items.filter {
      val dateDays = it.movie.released
      dateDays?.isEqual(now.minusDays(1)) == true
    }
    val last7DaysItems = (items - yesterdayItems).filter {
      val dateDays = it.movie.released
      dateDays?.isAfter(now.minusDays(8)) == true
    }
    val last30DaysItems = (items - yesterdayItems - last7DaysItems).filter {
      val dateDays = it.movie.released
      dateDays?.isAfter(now.minusDays(31)) == true
    }
    val last90Days = (items - yesterdayItems - last7DaysItems - last30DaysItems).filter {
      val dateDays = it.movie.released
      dateDays?.isAfter(now.minusDays(91)) == true
    }

    val itemsMap = mutableMapOf<Int, List<CalendarMovieListItem>>()
      .apply {
        put(R.string.textYesterday, yesterdayItems)
        put(R.string.textLast7Days, last7DaysItems)
        put(R.string.textLast30Days, last30DaysItems)
        put(R.string.textLast90Days, last90Days)
      }

    return itemsMap.entries.fold(mutableListOf()) { acc, entry ->
      acc.apply {
        if (entry.value.isNotEmpty()) {
          add(CalendarMovieListItem.Header.create(entry.key, CalendarMode.RECENTS))
          addAll(entry.value)
        }
      }
    }
  }
}
