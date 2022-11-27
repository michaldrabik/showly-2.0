package com.michaldrabik.ui_model

import com.michaldrabik.common.Config

enum class ImageType(
  val id: Int,
  val spanSize: Int,
  val key: String
) {
  POSTER(1, 1, "poster"),
  FANART(2, 2, "fanart"),
  FANART_WIDE(3, 3, "fanart"),
  TWITTER(4, 3, "twitterAd"),
  PREMIUM(5, 3, "premiumAd"),
  PROFILE(6, 1, "profile"),
  FILTERS(7, Config.LISTS_GRID_SPAN, "filters")
}
