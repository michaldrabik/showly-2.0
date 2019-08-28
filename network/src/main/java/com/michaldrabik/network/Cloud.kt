package com.michaldrabik.network

import com.michaldrabik.network.trakt.api.TraktApi
import com.michaldrabik.network.tvdb.api.TvdbApi
import javax.inject.Inject

class Cloud @Inject constructor(
  val traktApi: TraktApi,
  val tvdbApi: TvdbApi
)