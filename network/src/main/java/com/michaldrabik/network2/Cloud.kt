package com.michaldrabik.network2

import com.michaldrabik.network2.api.TraktApi
import javax.inject.Inject

class Cloud @Inject constructor(
  val traktApi: TraktApi
)