package com.michaldrabik.ui_progress_movies.calendar.helpers.groupers

import com.michaldrabik.common.extensions.toLocalZone
import com.michaldrabik.ui_model.CalendarMode
import com.michaldrabik.ui_progress_movies.R
import com.michaldrabik.ui_progress_movies.calendar.recycler.CalendarMovieListItem
import java.time.DayOfWeek
import java.time.Month
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters.next
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarFutureGrouper @Inject constructor() : CalendarGrouper {

  override fun groupByTime(
    nowUtc: ZonedDateTime,
    items: List<CalendarMovieListItem.MovieItem>
  ): List<CalendarMovieListItem> {
    val nowDays = nowUtc.toLocalZone().toLocalDate()

    val itemsMap = mutableMapOf<Int, MutableList<CalendarMovieListItem>>()
      .apply {
        put(R.string.textToday, mutableListOf())
        put(R.string.textTomorrow, mutableListOf())
        put(R.string.textThisWeek, mutableListOf())
        put(R.string.textNextWeek, mutableListOf())
        put(R.string.textThisMonth, mutableListOf())
        put(R.string.textNextMonth, mutableListOf())
        put(R.string.textThisYear, mutableListOf())
        put(R.string.textLater, mutableListOf())
      }

    items.forEach { item ->
      val itemDays = item.movie.released
      when {
        itemDays?.isEqual(nowDays) == true -> {
          itemsMap[R.string.textToday]?.add(item)
        }
        itemDays?.isEqual(nowDays.plusDays(1)) == true -> {
          itemsMap[R.string.textTomorrow]?.add(item)
        }
        itemDays?.isBefore(nowDays.with(next(DayOfWeek.MONDAY))) == true -> {
          itemsMap[R.string.textThisWeek]?.add(item)
        }
        itemDays?.isBefore(nowDays.plusWeeks(1).with(next(DayOfWeek.MONDAY))) == true -> {
          itemsMap[R.string.textNextWeek]?.add(item)
        }
        itemDays?.month == nowDays.month && itemDays?.year == nowDays.year -> {
          itemsMap[R.string.textThisMonth]?.add(item)
        }
        (itemDays?.monthValue == (nowDays.monthValue + 1) && itemDays.year == nowDays.year) ||
          (itemDays?.month == Month.JANUARY && nowDays.month == Month.DECEMBER) -> {
          itemsMap[R.string.textNextMonth]?.add(item)
        }
        itemDays?.year == nowDays.year -> {
          itemsMap[R.string.textThisYear]?.add(item)
        }
        else -> itemsMap[R.string.textLater]?.add(item)
      }
    }

    return itemsMap.entries.fold(mutableListOf()) { acc, entry ->
      acc.apply {
        if (entry.value.isNotEmpty()) {
          add(CalendarMovieListItem.Header.create(entry.key, CalendarMode.PRESENT_FUTURE))
          addAll(entry.value)
        }
      }
    }
  }
}
