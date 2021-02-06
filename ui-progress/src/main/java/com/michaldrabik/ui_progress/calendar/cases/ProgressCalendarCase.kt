package com.michaldrabik.ui_progress.calendar.cases

import androidx.annotation.StringRes
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.common.extensions.nowUtc
import com.michaldrabik.common.extensions.toLocalZone
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_progress.ProgressItem
import com.michaldrabik.ui_progress.R
import com.michaldrabik.ui_progress.calendar.cases.ProgressCalendarCase.Section.LATER
import com.michaldrabik.ui_progress.calendar.cases.ProgressCalendarCase.Section.NEXT_WEEK
import com.michaldrabik.ui_progress.calendar.cases.ProgressCalendarCase.Section.THIS_WEEK
import com.michaldrabik.ui_progress.calendar.cases.ProgressCalendarCase.Section.TODAY
import com.michaldrabik.ui_progress.calendar.cases.ProgressCalendarCase.Section.TOMORROW
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalTime
import org.threeten.bp.temporal.ChronoUnit.DAYS
import javax.inject.Inject

@AppScope
class ProgressCalendarCase @Inject constructor() {

  fun prepareItems(items: List<ProgressItem>): List<ProgressItem> {
    val today = nowUtc().toLocalZone()
    val calendarItems = items
      .filter { it.upcomingEpisode != Episode.EMPTY }
      .filter {
        val airTime = it.upcomingEpisode.firstAired?.toLocalZone()
        airTime != null && (airTime.truncatedTo(DAYS) == today.truncatedTo(DAYS) || airTime.isAfter(today))
      }
      .sortedBy { it.upcomingEpisode.firstAired }
      .toMutableList()

    return groupByTime(calendarItems)
  }

  private fun groupByTime(items: MutableList<ProgressItem>): List<ProgressItem> {
    val today = nowUtc().toLocalZone()
    val nextWeekStart = today.plusDays(((DayOfWeek.SUNDAY.value - today.dayOfWeek.value) + 1L))

    val timeMap = mutableMapOf<Section, MutableList<ProgressItem>>()
    val sectionsList = mutableListOf<ProgressItem>()

    items.forEach { item ->
      val time = item.upcomingEpisode.firstAired!!.toLocalZone()
      when {
        time.dayOfYear == today.dayOfYear ->
          timeMap.getOrPut(TODAY, { mutableListOf() }).add(item)
        time.dayOfYear == today.plusDays(1).dayOfYear ->
          timeMap.getOrPut(TOMORROW, { mutableListOf() }).add(item)
        time.with(LocalTime.NOON).isBefore(nextWeekStart.with(LocalTime.NOON)) ->
          timeMap.getOrPut(THIS_WEEK, { mutableListOf() }).add(item)
        time.with(LocalTime.NOON).isBefore(nextWeekStart.plusWeeks(1).with(LocalTime.NOON)) ->
          timeMap.getOrPut(NEXT_WEEK, { mutableListOf() }).add(item)
        else ->
          timeMap.getOrPut(LATER, { mutableListOf() }).add(item)
      }
    }

    timeMap.entries
      .sortedBy { it.key.order }
      .forEach { (section, items) ->
        sectionsList.run {
          add(ProgressItem.EMPTY.copy(headerTextResId = section.headerRes))
          addAll(items.toList())
        }
      }

    return sectionsList
  }

  enum class Section(@StringRes val headerRes: Int, val order: Int) {
    TODAY(R.string.textToday, 0),
    TOMORROW(R.string.textTomorrow, 1),
    THIS_WEEK(R.string.textThisWeek, 2),
    NEXT_WEEK(R.string.textNextWeek, 3),
    LATER(R.string.textLater, 4)
  }
}
