package com.michaldrabik.network

import com.michaldrabik.network.aws.api.AwsApi
import com.michaldrabik.network.di.CloudScope
import com.michaldrabik.network.trakt.api.TraktApi
import com.michaldrabik.network.tvdb.api.TvdbApi
import javax.inject.Inject

@CloudScope
class Cloud @Inject constructor(
  val traktApi: TraktApi,
  val tvdbApi: TvdbApi,
  val awsApi: AwsApi
)
