package com.michaldrabik.showly2.utilities.deeplink.resolvers

import com.michaldrabik.showly2.utilities.deeplink.DeepLinkSource
import com.michaldrabik.ui_model.IdImdb

class ImdbSourceResolver : SourceResolver {

  override fun resolve(linkPath: List<String>): DeepLinkSource? {
    if (linkPath.size < 2 || !linkPath[1].startsWith("tt") || linkPath[1].length <= 2) {
      return null
    }

    return DeepLinkSource.ImdbSource(IdImdb(linkPath[1]))
  }
}
