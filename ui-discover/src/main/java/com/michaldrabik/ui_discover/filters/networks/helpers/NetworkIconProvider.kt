package com.michaldrabik.ui_discover.filters.networks.helpers

import androidx.annotation.DrawableRes
import com.michaldrabik.ui_discover.R
import com.michaldrabik.ui_model.Network
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkIconProvider @Inject constructor() {

  @DrawableRes
  fun getIcon(network: Network): Int =
    when (network) {
      Network.ABC -> R.drawable.ic_abc
      Network.AMC -> R.drawable.ic_amc
      Network.APPLE -> R.drawable.ic_apple
      Network.AMAZON -> R.drawable.ic_amazon
      Network.BBC -> R.drawable.ic_bbc
      Network.CBS -> R.drawable.ic_cbs
      Network.CW -> R.drawable.ic_cw
      Network.DISCOVERY -> R.drawable.ic_discovery
      Network.DISNEY -> R.drawable.ic_disney
      Network.HBO -> R.drawable.ic_hbo
      Network.FOX -> R.drawable.ic_fox
      Network.HULU -> R.drawable.ic_hulu
      Network.ITV -> R.drawable.ic_itv
      Network.NBC -> R.drawable.ic_nbc
      Network.NETFLIX -> R.drawable.ic_netflix
      Network.SHOWTIME -> R.drawable.ic_showtime
      Network.RAKUTEN -> R.drawable.ic_rakuten
    }
}
