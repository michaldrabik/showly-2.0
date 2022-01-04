package com.michaldrabik.common

import java.util.concurrent.TimeUnit.DAYS
import java.util.concurrent.TimeUnit.HOURS
import java.util.concurrent.TimeUnit.MINUTES

object ConfigVariant {
  const val FIREBASE_SUFFIX = ""

  val SHOW_SYNC_COOLDOWN by lazy { HOURS.toMillis(12) }
  val MOVIE_SYNC_COOLDOWN by lazy { DAYS.toMillis(3) }
  val TRANSLATION_SYNC_SHOW_MOVIE_COOLDOWN by lazy { DAYS.toMillis(5) }
  val TRANSLATION_SYNC_EPISODE_COOLDOWN by lazy { DAYS.toMillis(3) }

  val RATINGS_CACHE_DURATION by lazy { DAYS.toMillis(3) }
  val STREAMINGS_CACHE_DURATION by lazy { DAYS.toMillis(3) }

  val REMOTE_CONFIG_FETCH_INTERVAL by lazy { MINUTES.toSeconds(60) }
  val TWITTER_AD_DELAY by lazy { DAYS.toMillis(5) }
  val PREMIUM_AD_DELAY by lazy { DAYS.toMillis(10) }
}
