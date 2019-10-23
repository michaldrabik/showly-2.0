package com.michaldrabik.showly2

import org.threeten.bp.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

object Config {
  const val TVDB_IMAGE_BASE_URL = "https://www.thetvdb.com/banners/"
  const val TVDB_IMAGE_BASE_POSTER_URL = "${TVDB_IMAGE_BASE_URL}_cache/posters/"
  const val TVDB_IMAGE_BASE_FANART_URL = "${TVDB_IMAGE_BASE_URL}fanart/original/"

  const val MY_SHOWS_RECENTS_AMOUNT = 6
  const val SEARCH_RECENTS_AMOUNT = 5
  const val IMAGE_FADE_DURATION_MS = 200

  val SHOW_DETAILS_CACHE_DURATION by lazy { TimeUnit.DAYS.toMillis(3) }
  val DISCOVER_SHOWS_CACHE_DURATION by lazy { TimeUnit.HOURS.toMillis(6) }
  val ACTORS_CACHE_DURATION by lazy { TimeUnit.DAYS.toMillis(7) }
  val RELATED_CACHE_DURATION by lazy { TimeUnit.DAYS.toMillis(7) }
  val DISPLAY_DATE_FORMAT: DateTimeFormatter by lazy { DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy 'at' HH:mm") }
}