package com.michaldrabik.network

internal object Config {
  const val TRAKT_VERSION = "2"
  const val TRAKT_BASE_URL = "https://api.trakt.tv/"
  const val TRAKT_CLIENT_ID = BuildConfig.TRAKT_CLIENT_ID

  const val TVDB_BASE_URL = "https://api.thetvdb.com/"
  const val TVDB_CLIENT_ID = BuildConfig.TVDB_CLIENT_ID
  const val TVDB_API_KEY = BuildConfig.TVDB_API_KEY
  const val TVDB_USER = BuildConfig.TVDB_USERNAME
}