package com.michaldrabik.common

import org.threeten.bp.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

object Config {
  private const val TVDB_IMAGE_PERSON_BASE_URL = "https://artworks.thetvdb.com"
  const val TVDB_IMAGE_BASE_BANNERS_URL = "$TVDB_IMAGE_PERSON_BASE_URL/banners/"

  const val DEVELOPER_MAIL = "showlyapp@gmail.com"
  const val PLAYSTORE_URL = "https://play.google.com/store/apps/details?id=com.michaldrabik.showly2"

  val MY_SHOWS_RECENTS_OPTIONS = arrayOf(2, 4, 6, 8)
  val DISCOVER_SHOWS_CACHE_DURATION by lazy { TimeUnit.HOURS.toMillis(12) }
  val RELATED_CACHE_DURATION by lazy { TimeUnit.DAYS.toMillis(7) }
  val SHOW_DETAILS_CACHE_DURATION by lazy { TimeUnit.DAYS.toMillis(3) }

  val DISPLAY_DATE_FORMAT: DateTimeFormatter by lazy { DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy 'at' HH:mm") }
  val DISPLAY_DATE_DAY_ONLY_FORMAT: DateTimeFormatter by lazy { DateTimeFormatter.ofPattern("dd MMM yyyy") }
}
