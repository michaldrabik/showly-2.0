package com.michaldrabik.ui_discover.helpers.itemtype

import com.michaldrabik.ui_model.ImageType

private const val BUFFER = 11

internal class TabletImageTypeProvider : ImageTypeProvider {

  override val twitterAdPosition = 14
  override val premiumAdPosition = 30

  override fun getImageType(position: Int): ImageType {
    if (position % BUFFER == 0) return ImageType.FANART_WIDE
    if ((position + (BUFFER - 10)) % BUFFER == 0) return ImageType.FANART_WIDE
    if ((position + (BUFFER - 2)) % BUFFER == 0) return ImageType.FANART
    if ((position + (BUFFER - 5)) % BUFFER == 0) return ImageType.FANART
    if ((position + (BUFFER - 8)) % BUFFER == 0) return ImageType.FANART
    return ImageType.POSTER
  }
}
