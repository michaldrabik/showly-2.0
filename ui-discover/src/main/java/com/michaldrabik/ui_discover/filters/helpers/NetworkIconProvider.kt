package com.michaldrabik.ui_discover.filters.helpers

import androidx.annotation.DrawableRes
import com.michaldrabik.ui_discover.R
import com.michaldrabik.ui_model.Network
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkIconProvider @Inject constructor() {

  @DrawableRes
  fun getIcon(network: Network): Int = when (network) {
    Network.ABC -> R.drawable.ic_abc
    Network.AMC -> R.drawable.ic_abc
    Network.APPLE -> R.drawable.ic_abc
    Network.AMAZON -> R.drawable.ic_abc
    Network.BBC -> R.drawable.ic_abc
    Network.CBS -> R.drawable.ic_abc
    Network.CW -> R.drawable.ic_abc
    Network.DISCOVERY -> R.drawable.ic_abc
    Network.DISNEY -> R.drawable.ic_abc
    Network.HBO -> R.drawable.ic_abc
    Network.FOX -> R.drawable.ic_abc
    Network.HULU -> R.drawable.ic_abc
    Network.ITV -> R.drawable.ic_abc
    Network.NBC -> R.drawable.ic_abc
    Network.NETFLIX -> R.drawable.ic_abc
    Network.SHOWTIME -> R.drawable.ic_showtime
    Network.RAKUTEN -> R.drawable.ic_abc
  }
}
