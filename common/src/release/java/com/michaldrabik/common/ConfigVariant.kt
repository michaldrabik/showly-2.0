package com.michaldrabik.common

import java.util.concurrent.TimeUnit.DAYS
import java.util.concurrent.TimeUnit.HOURS

object ConfigVariant {
  const val FIREBASE_SUFFIX = ""

  val SHOW_SYNC_COOLDOWN by lazy { HOURS.toMillis(12) }
  val MOVIE_SYNC_COOLDOWN by lazy { DAYS.toMillis(3) }
  val TRANSLATION_SYNC_COOLDOWN by lazy { DAYS.toMillis(7) }
}
