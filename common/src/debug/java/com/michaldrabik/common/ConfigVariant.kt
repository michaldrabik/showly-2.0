package com.michaldrabik.common

import java.util.concurrent.TimeUnit.MINUTES

object ConfigVariant {
  const val FIREBASE_SUFFIX = "-debug"

  val SHOW_SYNC_COOLDOWN by lazy { MINUTES.toMillis(5) }
  val MOVIE_SYNC_COOLDOWN by lazy { MINUTES.toMillis(5) }
  val TRANSLATION_SYNC_COOLDOWN by lazy { MINUTES.toMillis(60) }

  val RATINGS_CACHE_DURATION by lazy { MINUTES.toMillis(3) }
  val STREAMINGS_CACHE_DURATION by lazy { MINUTES.toMillis(3) }

  val REMOTE_CONFIG_FETCH_INTERVAL by lazy { MINUTES.toSeconds(3) }
}
