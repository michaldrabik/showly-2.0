package com.michaldrabik.data_remote.trakt.model

@Suppress("PropertyName")
data class SyncActivity(
  val all: String,
  val shows: Shows,
  val movies: Movies,
  val episodes: Episodes,
  val lists: Lists,
) {

  data class Shows(
    val hidden_at: String,
    val watchlisted_at: String,
  )

  data class Movies(
    val watched_at: String,
    val hidden_at: String,
    val watchlisted_at: String,
  )

  data class Episodes(
    val watched_at: String
  )

  data class Watchlist(
    val updated_at: String
  )

  data class Lists(
    val updated_at: String
  )
}
