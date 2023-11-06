package com.michaldrabik.ui_model

enum class ImageType(
  val id: Int,
  val key: String
) {
  POSTER(1, "poster"),
  FANART(2, "fanart"),
  FANART_WIDE(3, "fanart"),
  TWITTER(4, "twitterAd"),
  PREMIUM(5, "premiumAd"),
  PROFILE(6, "profile");

  fun getSpan(isTablet: Boolean): Int {
    return when (this) {
      POSTER -> 1
      FANART -> 2
      FANART_WIDE -> if (isTablet) 3 else 3
      TWITTER -> if (isTablet) 6 else 3
      PREMIUM -> if (isTablet) 6 else 3
      PROFILE -> 1
    }
  }
}
