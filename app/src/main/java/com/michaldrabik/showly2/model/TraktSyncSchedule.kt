package com.michaldrabik.showly2.model

import androidx.annotation.StringRes
import com.michaldrabik.showly2.R
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.DAYS
import java.util.concurrent.TimeUnit.SECONDS

enum class TraktSyncSchedule(
  val duration: Long,
  val durationUnit: TimeUnit,
  @StringRes val stringRes: Int,
  @StringRes val confirmationStringRes: Int,
  @StringRes val buttonStringRes: Int
) {
  OFF(0, SECONDS, R.string.textTraktSyncOptionOff, R.string.textTraktSyncOptionOffMessage, R.string.textTraktSyncSchedule),
  EVERY_DAY(1, DAYS, R.string.textTraktSyncOptionDaily, R.string.textTraktSyncOptionDailyMessage, R.string.textTraktSyncOptionDailyButton),
  EVERY_3_DAYS(3, DAYS, R.string.textTraktSyncOption3Day, R.string.textTraktSyncOption3DayMessage, R.string.textTraktSyncOption3DayButton),
  EVERY_WEEK(7, DAYS, R.string.textTraktSyncOptionWeekly, R.string.textTraktSyncOptionWeeklyMessage, R.string.textTraktSyncOptionWeeklyButton)
}
