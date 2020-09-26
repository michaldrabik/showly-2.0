package com.michaldrabik.showly2.ui.main.cases

import android.content.SharedPreferences
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.common.extensions.nowUtcMillis
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

@AppScope
class MainRateAppCase @Inject constructor(
  @Named("miscPreferences") private var miscPreferences: SharedPreferences
) {

  companion object {
    const val KEY_RATE_APP_COUNT = "KEY_RATE_APP_COUNT"
    const val KEY_RATE_APP_TIMESTAMP = "KEY_RATE_APP_TIMESTAMP"

    const val MAX_COUNT = 3
  }

  fun shouldShowRateApp(): Boolean {
    val count = miscPreferences.getInt(KEY_RATE_APP_COUNT, 0)
    val timestamp = miscPreferences.getLong(KEY_RATE_APP_TIMESTAMP, -1)

    if (timestamp == -1L) {
      updateTimestamp(count)
      return false
    }

    if (count < (MAX_COUNT - 1) && nowUtcMillis() - timestamp > TimeUnit.DAYS.toMillis(10)) {
      updateTimestamp(count + 1)
      return true
    }

    if (count < MAX_COUNT && nowUtcMillis() - timestamp > TimeUnit.DAYS.toMillis(14)) {
      updateTimestamp(count + 1)
      return true
    }

    return false
  }

  private fun updateTimestamp(count: Int) {
    miscPreferences.edit().apply {
      putInt(KEY_RATE_APP_COUNT, count)
      putLong(KEY_RATE_APP_TIMESTAMP, nowUtcMillis())
      apply()
    }
  }

  fun finalize() = updateTimestamp(MAX_COUNT)
}
