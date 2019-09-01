package com.michaldrabik.showly2.model

enum class ImageType(val spanSize: Int, val key: String) {
  POSTER(1, "poster"),
  FANART(2, "fanart"),
  FANART_WIDE(3, "fanart"),
}