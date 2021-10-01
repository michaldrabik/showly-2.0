package com.michaldrabik.showly2.utilities.deeplink

import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Show

data class DeepLinkBundle(
  val show: Show? = null,
  val movie: Movie? = null
) {

  companion object {
    val EMPTY = DeepLinkBundle()
  }
}
