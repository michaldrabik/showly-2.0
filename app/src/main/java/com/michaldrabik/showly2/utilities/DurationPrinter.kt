package com.michaldrabik.showly2.utilities

import android.content.Context
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.utilities.extensions.nowUtc
import org.threeten.bp.Duration
import org.threeten.bp.ZonedDateTime

class DurationPrinter(private val context: Context) {

  fun print(date: ZonedDateTime?): String {
    if (date == null) return "TBA"

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
      return context.resources.getQuantityString(R.plurals.textHoursToAir, hours, hours)
    }
    return context.resources.getQuantityString(R.plurals.textDaysToAir, days, days)
  }
}