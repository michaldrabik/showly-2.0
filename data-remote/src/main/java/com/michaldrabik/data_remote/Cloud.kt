package com.michaldrabik.data_remote

import com.michaldrabik.data_remote.aws.api.AwsApi
import com.michaldrabik.data_remote.di.CloudScope
import com.michaldrabik.data_remote.reddit.api.RedditApi
import com.michaldrabik.data_remote.tmdb.api.TmdbApi
import com.michaldrabik.data_remote.trakt.api.TraktApi
import javax.inject.Inject

@CloudScope
class Cloud @Inject constructor(
  val traktApi: TraktApi,
  val tmdbApi: TmdbApi,
  val awsApi: AwsApi,
  val redditApi: RedditApi,
)
