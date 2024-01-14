package com.michaldrabik.ui_base.utilities

import android.content.Context
import com.michaldrabik.common.extensions.nowUtc
import com.michaldrabik.ui_base.R
import java.time.Duration
import java.time.ZonedDateTime

class DurationPrinter(private val context: Context) {

  fun print(date: ZonedDateTime?): String {
    if (date == null) return context.getString(R.string.textTba)

    val duration = Duration.between(nowUtc(), date)
    if (duration.isNegative) return context.getString(R.string.textAiredAlready)

    val days = duration.toDays().toInt()
    if (days == 0) {
      val hours = duration.toHours().toInt()
      if (hours == 0) {
        val minutes = duration.toMinutes().toInt()
        if (minutes == 0) {
          return context.getString(R.string.textAirsNow)
        }
        return context.resources.getQuantityString(R.plurals.textMinutesToAir, minutes, minutes)
      }
      return context.resources.getQuantityString(R.plurals.textHoursToAir, hours + 1, hours + 1)
    }
    return context.resources.getQuantityString(R.plurals.textDaysToAir, days + 1, days + 1)
  }
}
