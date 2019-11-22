package com.michaldrabik.showly2.model

import androidx.annotation.StringRes
import com.michaldrabik.showly2.R

@Suppress("unused")
enum class NotificationDelay(
  @StringRes val stringRes: Int,
  val delayMs: Long
) {
  WHEN_AVAILABLE(R.string.textSettingsShowsNotificationsWhenAvailable, 0),
  HOURS_1(R.string.textSettingsShowsNotificationsWhen1Hour, 3_600_000),
  HOURS_3(R.string.textSettingsShowsNotificationsWhen3Hours, 3_600_000 * 3),
  HOURS_6(R.string.textSettingsShowsNotificationsWhen6Hours, 3_600_000 * 6),
  HOURS_12(R.string.textSettingsShowsNotificationsWhen12Hours, 3_600_000 * 12),
  HOURS_24(R.string.textSettingsShowsNotificationsWhenNextDay, 3_600_000 * 24);

  companion object {
    fun fromDelay(delayMs: Long) =
      enumValues<NotificationDelay>().first { it.delayMs == delayMs }
  }
}