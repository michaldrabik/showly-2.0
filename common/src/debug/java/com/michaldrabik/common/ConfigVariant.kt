package com.michaldrabik.common

import java.util.concurrent.TimeUnit.MINUTES

object ConfigVariant {
  const val FIREBASE_SUFFIX = "-debug"

  val SHOW_SYNC_COOLDOWN by lazy { MINUTES.toMillis(5) }
  val MOVIE_SYNC_COOLDOWN by lazy { MINUTES.toMillis(5) }
  val TRANSLATION_SYNC_COOLDOWN by lazy { MINUTES.toMillis(60) }
}
