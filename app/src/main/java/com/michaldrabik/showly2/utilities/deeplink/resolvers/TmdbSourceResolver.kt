package com.michaldrabik.showly2.utilities.deeplink.resolvers

import androidx.core.text.isDigitsOnly
import com.michaldrabik.showly2.utilities.deeplink.DeepLinkResolver
import com.michaldrabik.showly2.utilities.deeplink.DeepLinkSource
import com.michaldrabik.ui_model.IdTmdb

class TmdbSourceResolver : SourceResolver {

  override fun resolve(linkPath: List<String>): DeepLinkSource? {
    if (linkPath.size < 2 ||
      !(linkPath[0] == DeepLinkResolver.TMDB_TYPE_TV || linkPath[0] == DeepLinkResolver.TMDB_TYPE_MOVIE) ||
      linkPath[1].length <= 1
    ) {
      return null
    }

    val id = linkPath[1].substringBefore("-").trim()
    val type = linkPath[0]
    return if (id.isDigitsOnly()) {
      DeepLinkSource.TmdbSource(IdTmdb(id.toLong()), type)
    } else {
      null
    }
  }
}
