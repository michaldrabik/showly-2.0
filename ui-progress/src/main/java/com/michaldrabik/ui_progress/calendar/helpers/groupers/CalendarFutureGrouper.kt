package com.michaldrabik.ui_progress.calendar.helpers.groupers

import com.michaldrabik.common.extensions.nowUtc
import com.michaldrabik.common.extensions.toLocalZone
import com.michaldrabik.ui_progress.R
import com.michaldrabik.ui_progress.calendar.helpers.CalendarMode
import com.michaldrabik.ui_progress.calendar.recycler.CalendarListItem
import org.threeten.bp.DayOfWeek
import org.threeten.bp.Month
import org.threeten.bp.temporal.ChronoUnit.DAYS
import org.threeten.bp.temporal.TemporalAdjusters.next
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarFutureGrouper @Inject constructor() : CalendarGrouper {

  override fun groupByTime(items: List<CalendarListItem.Episode>): List<CalendarListItem> {
    val nowDays = nowUtc().toLocalZone().truncatedTo(DAYS)

    val itemsMap = mutableMapOf<Int, MutableList<CalendarListItem>>()
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
      val dateDays = item.episode.firstAired?.toLocalZone()?.truncatedTo(DAYS)
      when {
        dateDays?.isEqual(nowDays) == true -> {
          itemsMap[R.string.textToday]?.add(item)
        }
        dateDays?.isEqual(nowDays.plusDays(1)) == true -> {
          itemsMap[R.string.textTomorrow]?.add(item)
        }
        dateDays?.isBefore(nowDays.with(next(DayOfWeek.MONDAY))) == true -> {
          itemsMap[R.string.textThisWeek]?.add(item)
        }
        dateDays?.isBefore(nowDays.plusWeeks(1).with(next(DayOfWeek.MONDAY))) == true -> {
          itemsMap[R.string.textNextWeek]?.add(item)
        }
        dateDays?.month == nowDays.month && dateDays?.year == nowDays.year -> {
          itemsMap[R.string.textThisMonth]?.add(item)
        }
        dateDays?.monthValue == (nowDays.monthValue + 1) || (dateDays?.month == Month.JANUARY && nowDays.month == Month.DECEMBER) -> {
          itemsMap[R.string.textNextMonth]?.add(item)
        }
        dateDays?.year == nowDays.year -> {
          itemsMap[R.string.textThisYear]?.add(item)
        }
        else -> itemsMap[R.string.textLater]?.add(item)
      }
    }

    return itemsMap.entries.fold(mutableListOf(), { acc, entry ->
      acc.apply {
        if (entry.value.isNotEmpty()) {
          add(CalendarListItem.Header.create(entry.key, CalendarMode.PRESENT_FUTURE))
          addAll(entry.value)
        }
      }
    })
  }
}
