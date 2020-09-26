package com.michaldrabik.common

import org.threeten.bp.format.DateTimeFormatter

object Config {
  val DISPLAY_DATE_FORMAT: DateTimeFormatter by lazy { DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy 'at' HH:mm") }
  val DISPLAY_DATE_DAY_ONLY_FORMAT: DateTimeFormatter by lazy { DateTimeFormatter.ofPattern("dd MMM yyyy") }
}
