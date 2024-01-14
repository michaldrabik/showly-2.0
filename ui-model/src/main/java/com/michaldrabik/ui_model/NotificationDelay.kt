package com.michaldrabik.ui_model

import androidx.annotation.StringRes

const val HOUR_MS = 3_600_000L

@Suppress("unused")
enum class NotificationDelay(
  @StringRes val stringRes: Int,
  val delayMs: Long
) {
  HOURS_12_NEG(R.string.textSettingsShowsNotificationsWhen12HoursBefore, -HOUR_MS * 12),
  HOURS_6_NEG(R.string.textSettingsShowsNotificationsWhen6HoursBefore, -HOUR_MS * 6),
  HOURS_3_NEG(R.string.textSettingsShowsNotificationsWhen3HoursBefore, -HOUR_MS * 3),
  HOURS_1_NEG(R.string.textSettingsShowsNotificationsWhen1HourBefore, -HOUR_MS),
  WHEN_AVAILABLE(R.string.textSettingsShowsNotificationsWhenAvailable, 0),
  HOURS_1(R.string.textSettingsShowsNotificationsWhen1Hour, HOUR_MS),
  HOURS_3(R.string.textSettingsShowsNotificationsWhen3Hours, HOUR_MS * 3),
  HOURS_6(R.string.textSettingsShowsNotificationsWhen6Hours, HOUR_MS * 6),
  HOURS_12(R.string.textSettingsShowsNotificationsWhen12Hours, HOUR_MS * 12),
  HOURS_24(R.string.textSettingsShowsNotificationsWhenNextDay, HOUR_MS * 24);

  companion object {
    fun fromDelay(delayMs: Long) =
      enumValues<NotificationDelay>().first { it.delayMs == delayMs }
  }

  fun isBefore() = this in listOf(HOURS_1_NEG, HOURS_3_NEG, HOURS_6_NEG, HOURS_12_NEG)
}
