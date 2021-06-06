package com.michaldrabik.showly2.ui.main.cases

import android.content.SharedPreferences
import com.michaldrabik.common.extensions.nowUtcMillis
import dagger.hilt.android.scopes.ViewModelScoped
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

@ViewModelScoped
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
    val isPastTwoWeeks = nowUtcMillis() - timestamp > TimeUnit.DAYS.toMillis(14)

    if (timestamp == -1L) {
      updateTimestamp(count)
      return false
    }

    if (count < MAX_COUNT && isPastTwoWeeks) {
      updateTimestamp(count)
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

  fun complete() {
    val count = miscPreferences.getInt(KEY_RATE_APP_COUNT, 0)
    updateTimestamp(count + 1)
  }
}
