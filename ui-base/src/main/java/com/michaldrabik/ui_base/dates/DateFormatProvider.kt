package com.michaldrabik.ui_base.dates

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.ui_base.dates.AppDateFormat.DEFAULT_12
import com.michaldrabik.ui_base.dates.AppDateFormat.DEFAULT_24
import com.michaldrabik.ui_base.dates.AppDateFormat.TRAKT_12
import com.michaldrabik.ui_base.dates.AppDateFormat.TRAKT_24
import com.michaldrabik.ui_repository.SettingsRepository
import org.threeten.bp.format.DateTimeFormatter
import javax.inject.Inject

@AppScope
class DateFormatProvider @Inject constructor(
  private val settingsRepository: SettingsRepository,
) {

  companion object {
    fun loadSettingsFormat(format: AppDateFormat): DateTimeFormatter =
      when (format) {
        DEFAULT_12 -> DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy, h:mm a")
        DEFAULT_24 -> DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy, HH:mm")
        TRAKT_12 -> DateTimeFormatter.ofPattern("MMM dd, yyyy h:mm a (EEEE)")
        TRAKT_24 -> DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm (EEEE)")
      }
  }

  fun loadShortDayFormat(): DateTimeFormatter =
    when (AppDateFormat.valueOf(settingsRepository.getDateFormat())) {
      DEFAULT_12 -> DateTimeFormatter.ofPattern("dd MMM yyyy")
      DEFAULT_24 -> DateTimeFormatter.ofPattern("dd MMM yyyy")
      TRAKT_12 -> DateTimeFormatter.ofPattern("MMM dd, yyyy")
      TRAKT_24 -> DateTimeFormatter.ofPattern("MMM dd, yyyy")
    }

  fun loadFullDayFormat(): DateTimeFormatter =
    when (AppDateFormat.valueOf(settingsRepository.getDateFormat())) {
      DEFAULT_12 -> DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")
      DEFAULT_24 -> DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")
      TRAKT_12 -> DateTimeFormatter.ofPattern("MMMM dd, yyyy (EEEE)")
      TRAKT_24 -> DateTimeFormatter.ofPattern("MMMM dd, yyyy (EEEE)")
    }

  fun loadFullHourFormat(): DateTimeFormatter =
    when (AppDateFormat.valueOf(settingsRepository.getDateFormat())) {
      DEFAULT_12 -> DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy, h:mm a")
      DEFAULT_24 -> DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy, HH:mm")
      TRAKT_12 -> DateTimeFormatter.ofPattern("MMM dd, yyyy h:mm a (EEEE)")
      TRAKT_24 -> DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm (EEEE)")
    }
}
