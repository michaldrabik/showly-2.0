package com.michaldrabik.ui_progress.calendar.helpers.groupers

import com.michaldrabik.common.extensions.nowUtc
import com.michaldrabik.common.extensions.toLocalZone
import com.michaldrabik.ui_model.CalendarMode
import com.michaldrabik.ui_progress.R
import com.michaldrabik.ui_progress.calendar.recycler.CalendarListItem
import java.time.DayOfWeek
import java.time.Month
import java.time.temporal.ChronoUnit.DAYS
import java.time.temporal.TemporalAdjusters.next
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
      val itemDays = item.episode.firstAired?.toLocalZone()?.truncatedTo(DAYS)
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
          add(CalendarListItem.Header.create(entry.key, CalendarMode.PRESENT_FUTURE))
          addAll(entry.value)
        }
      }
    }
  }
}
