package com.michaldrabik.ui_discover.helpers.itemtype

import com.michaldrabik.ui_model.ImageType

internal interface ImageTypeProvider {

  val twitterAdPosition: Int
  val premiumAdPosition: Int

  fun getImageType(position: Int): ImageType
}
