package com.michaldrabik.ui_base.dates

import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.dates.AppDateFormat.DEFAULT_12
import com.michaldrabik.ui_base.dates.AppDateFormat.DEFAULT_24
import com.michaldrabik.ui_base.dates.AppDateFormat.MISC_12
import com.michaldrabik.ui_base.dates.AppDateFormat.MISC_24
import com.michaldrabik.ui_base.dates.AppDateFormat.TRAKT_12
import com.michaldrabik.ui_base.dates.AppDateFormat.TRAKT_24
import com.michaldrabik.ui_base.dates.AppDateFormat.valueOf
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DateFormatProvider @Inject constructor(
  private val settingsRepository: SettingsRepository,
) {

  companion object {
    const val DAY_1 = "dd MMM yyyy"
    const val DAY_2 = "MMM dd, yyyy"
    const val DAY_3 = "EEEE, dd MMMM yyyy"
    const val DAY_4 = "MMMM dd, yyyy (EEEE)"
    const val DAY_5 = "dd MMMM yyyy (EEEE)"

    const val DAY_HOUR_1 = "EEEE, dd MMM yyyy, h:mm a"
    const val DAY_HOUR_2 = "EEEE, dd MMM yyyy, HH:mm"
    const val DAY_HOUR_3 = "MMM dd, yyyy h:mm a (EEEE)"
    const val DAY_HOUR_4 = "MMM dd, yyyy HH:mm (EEEE)"
    const val DAY_HOUR_5 = "dd MMM yyyy, h:mm a (EEEE)"
    const val DAY_HOUR_6 = "dd MMM yyyy, HH:mm (EEEE)"

    fun loadSettingsFormat(
      format: AppDateFormat,
      language: String,
    ): DateTimeFormatter {
      val pattern = when (format) {
        DEFAULT_12 -> DAY_HOUR_1
        DEFAULT_24 -> DAY_HOUR_2
        TRAKT_12 -> DAY_HOUR_3
        TRAKT_24 -> DAY_HOUR_4
        MISC_12 -> DAY_HOUR_5
        MISC_24 -> DAY_HOUR_6
      }
      if (language == "zh") {
        return DateTimeFormatter.ofPattern(pattern.appendChineseDay())
      }
      return DateTimeFormatter.ofPattern(pattern)
    }
  }

  fun loadShortDayFormat(): DateTimeFormatter {
    val pattern = when (valueOf(settingsRepository.dateFormat)) {
      DEFAULT_12 -> DAY_1
      DEFAULT_24 -> DAY_1
      TRAKT_12 -> DAY_2
      TRAKT_24 -> DAY_2
      MISC_12 -> DAY_1
      MISC_24 -> DAY_1
    }
    return createDateFormat(pattern)
  }

  fun loadFullDayFormat(): DateTimeFormatter {
    val pattern = when (valueOf(settingsRepository.dateFormat)) {
      DEFAULT_12 -> DAY_3
      DEFAULT_24 -> DAY_3
      TRAKT_12 -> DAY_4
      TRAKT_24 -> DAY_4
      MISC_12 -> DAY_5
      MISC_24 -> DAY_5
    }
    return createDateFormat(pattern)
  }

  fun loadFullHourFormat(): DateTimeFormatter {
    val pattern = when (valueOf(settingsRepository.dateFormat)) {
      DEFAULT_12 -> DAY_HOUR_1
      DEFAULT_24 -> DAY_HOUR_2
      TRAKT_12 -> DAY_HOUR_3
      TRAKT_24 -> DAY_HOUR_4
      MISC_12 -> DAY_HOUR_5
      MISC_24 -> DAY_HOUR_6
    }
    return createDateFormat(pattern)
  }

  private fun createDateFormat(pattern: String): DateTimeFormatter {
    val language = settingsRepository.language
    if (language == "zh") {
      return DateTimeFormatter.ofPattern(pattern.appendChineseDay())
    }
    return DateTimeFormatter.ofPattern(pattern)
  }
}

// Adding special symbol in case of Chinese language as it is missing from DateTimeFormatter implementation.
private fun String.appendChineseDay(): String {
  return this.replace("dd", "dd\'日\'")
}
