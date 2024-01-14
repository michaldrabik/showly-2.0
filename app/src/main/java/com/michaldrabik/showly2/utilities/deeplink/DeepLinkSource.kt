package com.michaldrabik.showly2.utilities.deeplink

import com.michaldrabik.ui_model.IdImdb
import com.michaldrabik.ui_model.IdSlug
import com.michaldrabik.ui_model.IdTmdb

sealed class DeepLinkSource {

  data class ImdbSource(val id: IdImdb) : DeepLinkSource()

  data class TmdbSource(val id: IdTmdb, val type: String) : DeepLinkSource()

  data class TraktSource(val id: IdSlug, val type: String) : DeepLinkSource()
}
