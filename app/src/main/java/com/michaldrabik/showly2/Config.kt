package com.michaldrabik.showly2

object Config {
  const val TVDB_IMAGE_BASE_URL = "https://www.thetvdb.com/banners/"
  const val TVDB_IMAGE_BASE_POSTER_URL = "${TVDB_IMAGE_BASE_URL}_cache/posters/"
  const val TVDB_IMAGE_BASE_FANART_URL = "${TVDB_IMAGE_BASE_URL}fanart/original/"
  const val DISCOVER_SHOWS_CACHE_DURATION = 21_600_000 //6 hours
}