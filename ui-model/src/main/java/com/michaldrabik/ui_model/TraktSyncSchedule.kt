package com.michaldrabik.ui_model

import androidx.annotation.StringRes
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.DAYS
import java.util.concurrent.TimeUnit.HOURS
import java.util.concurrent.TimeUnit.SECONDS

enum class TraktSyncSchedule(
  val duration: Long,
  val durationUnit: TimeUnit,
  @StringRes val stringRes: Int,
  @StringRes val confirmationStringRes: Int,
  @StringRes val buttonStringRes: Int
) {
  OFF(0, SECONDS, R.string.textTraktSyncOptionOff, R.string.textTraktSyncOptionOffMessage, R.string.textTraktSyncSchedule),
  EVERY_HOUR(1, HOURS, R.string.textTraktSyncOption1Hour, R.string.textTraktSyncOptionConfirmMessage, R.string.textTraktSyncOptionHourButton),
  EVERY_3_HOURS(3, HOURS, R.string.textTraktSyncOption3Hours, R.string.textTraktSyncOptionConfirmMessage, R.string.textTraktSyncOption3HoursButton),
  EVERY_6_HOURS(6, HOURS, R.string.textTraktSyncOption6Hours, R.string.textTraktSyncOptionConfirmMessage, R.string.textTraktSyncOption6HoursButton),
  EVERY_DAY(1, DAYS, R.string.textTraktSyncOptionDaily, R.string.textTraktSyncOptionConfirmMessage, R.string.textTraktSyncOptionDailyButton),
  EVERY_3_DAYS(3, DAYS, R.string.textTraktSyncOption3Day, R.string.textTraktSyncOptionConfirmMessage, R.string.textTraktSyncOption3DayButton),
  EVERY_WEEK(7, DAYS, R.string.textTraktSyncOptionWeekly, R.string.textTraktSyncOptionConfirmMessage, R.string.textTraktSyncOptionWeeklyButton)
}
