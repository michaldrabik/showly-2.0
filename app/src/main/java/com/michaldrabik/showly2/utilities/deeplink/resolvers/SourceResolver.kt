package com.michaldrabik.showly2.utilities.deeplink.resolvers

import com.michaldrabik.showly2.utilities.deeplink.DeepLinkSource

interface SourceResolver {
  fun resolve(linkPath: List<String>): DeepLinkSource?
}
