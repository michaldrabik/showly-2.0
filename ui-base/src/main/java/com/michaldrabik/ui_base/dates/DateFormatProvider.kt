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
    fun loadSettingsFormat(format: AppDateFormat): DateTimeFormatter =
      when (format) {
        DEFAULT_12 -> DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy, h:mm a")
        DEFAULT_24 -> DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy, HH:mm")
        TRAKT_12 -> DateTimeFormatter.ofPattern("MMM dd, yyyy h:mm a (EEEE)")
        TRAKT_24 -> DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm (EEEE)")
        MISC_24 -> DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm (EEEE)")
        MISC_12 -> DateTimeFormatter.ofPattern("dd MMM yyyy, h:mm a (EEEE)")
      }
  }

  private val dayPattern1 by lazy { DateTimeFormatter.ofPattern("dd MMM yyyy") }
  private val dayPattern2 by lazy { DateTimeFormatter.ofPattern("MMM dd, yyyy") }
  private val dayPattern3 by lazy { DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy") }
  private val dayPattern4 by lazy { DateTimeFormatter.ofPattern("MMMM dd, yyyy (EEEE)") }
  private val dayPattern5 by lazy { DateTimeFormatter.ofPattern("dd MMMM yyyy (EEEE)") }

  private val hourPattern1 by lazy { DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy, h:mm a") }
  private val hourPattern2 by lazy { DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy, HH:mm") }
  private val hourPattern3 by lazy { DateTimeFormatter.ofPattern("MMM dd, yyyy h:mm a (EEEE)") }
  private val hourPattern4 by lazy { DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm (EEEE)") }
  private val hourPattern5 by lazy { DateTimeFormatter.ofPattern("dd MMM yyyy, h:mm a (EEEE)") }
  private val hourPattern6 by lazy { DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm (EEEE)") }

  fun loadShortDayFormat(): DateTimeFormatter =
    when (valueOf(settingsRepository.dateFormat)) {
      DEFAULT_12 -> dayPattern1
      DEFAULT_24 -> dayPattern1
      TRAKT_12 -> dayPattern2
      TRAKT_24 -> dayPattern2
      MISC_12 -> dayPattern1
      MISC_24 -> dayPattern1
    }

  fun loadFullDayFormat(): DateTimeFormatter =
    when (valueOf(settingsRepository.dateFormat)) {
      DEFAULT_12 -> dayPattern3
      DEFAULT_24 -> dayPattern3
      TRAKT_12 -> dayPattern4
      TRAKT_24 -> dayPattern4
      MISC_12 -> dayPattern5
      MISC_24 -> dayPattern5
    }

  fun loadFullHourFormat(): DateTimeFormatter =
    when (valueOf(settingsRepository.dateFormat)) {
      DEFAULT_12 -> hourPattern1
      DEFAULT_24 -> hourPattern2
      TRAKT_12 -> hourPattern3
      TRAKT_24 -> hourPattern4
      MISC_12 -> hourPattern5
      MISC_24 -> hourPattern6
    }
}
