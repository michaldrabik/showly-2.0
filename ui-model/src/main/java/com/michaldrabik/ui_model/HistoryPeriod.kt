package com.michaldrabik.ui_model

import androidx.annotation.StringRes

enum class HistoryPeriod(@StringRes val displayStringRes: Int) {
  THIS_WEEK(R.string.textPeriodThisWeek),
  LAST_WEEK(R.string.textPeriodLastWeek),
  THIS_MONTH(R.string.textPeriodThisMonth),
  LAST_MONTH(R.string.textPeriodLastMonth),
  LAST_30_DAYS(R.string.textPeriodLast30Days),
  LAST_90_DAYS(R.string.textPeriodLast90Days),
  LAST_365_DAYS(R.string.textPeriodLast365Days),
  ALL_TIME(R.string.textPeriodAllTime)
}
