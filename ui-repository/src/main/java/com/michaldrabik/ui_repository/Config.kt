package com.michaldrabik.ui_repository

import java.util.concurrent.TimeUnit

object Config {
  val DISCOVER_SHOWS_CACHE_DURATION by lazy { TimeUnit.HOURS.toMillis(12) }
  val RELATED_CACHE_DURATION by lazy { TimeUnit.DAYS.toMillis(7) }
  val SHOW_DETAILS_CACHE_DURATION by lazy { TimeUnit.DAYS.toMillis(3) }
}
