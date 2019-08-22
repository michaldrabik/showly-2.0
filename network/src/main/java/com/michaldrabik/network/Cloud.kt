package com.michaldrabik.network

import com.michaldrabik.network.trakt.api.TraktApi
import javax.inject.Inject

class Cloud @Inject constructor(
  val traktApi: TraktApi
)