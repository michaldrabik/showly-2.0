package com.michaldrabik.showly2

import org.threeten.bp.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

object Config {
  private const val TVDB_IMAGE_PERSON_BASE_URL = "https://artworks.thetvdb.com"
  const val TVDB_IMAGE_BASE_BANNERS_URL = "${TVDB_IMAGE_PERSON_BASE_URL}/banners/"
  const val TVDB_IMAGE_BASE_POSTER_URL = "${TVDB_IMAGE_BASE_BANNERS_URL}posters/"
  const val TVDB_IMAGE_BASE_FANART_URL = "${TVDB_IMAGE_BASE_BANNERS_URL}fanart/original/"

  const val PULL_TO_REFRESH_COOLDOWN_MS = 15_000
  const val SEARCH_RECENTS_AMOUNT = 5
  const val IMAGE_FADE_DURATION_MS = 200
  const val MY_SHOWS_RECENTS_DEFAULT = 6
  const val FANART_GALLERY_IMAGES_LIMIT = 30
  val MY_SHOWS_RECENTS_OPTIONS = arrayOf(2, 4, 6, 8)

  val SHOW_DETAILS_CACHE_DURATION by lazy { TimeUnit.DAYS.toMillis(3) }
  val DISCOVER_SHOWS_CACHE_DURATION by lazy { TimeUnit.HOURS.toMillis(10) }
  val ACTORS_CACHE_DURATION by lazy { TimeUnit.DAYS.toMillis(7) }
  val RELATED_CACHE_DURATION by lazy { TimeUnit.DAYS.toMillis(7) }
  val NEW_BADGE_DURATION by lazy { TimeUnit.HOURS.toMillis(30) }
  val SHOW_SYNC_COOLDOWN by lazy {
    if (BuildConfig.DEBUG) TimeUnit.MINUTES.toMillis(1) else TimeUnit.HOURS.toMillis(12)
  }
  val DISPLAY_DATE_FORMAT: DateTimeFormatter by lazy { DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy 'at' HH:mm") }
  val DISPLAY_DATE_SHORT_FORMAT: DateTimeFormatter by lazy { DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy") }
}