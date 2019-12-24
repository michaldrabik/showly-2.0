@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package com.michaldrabik.showly2.model

data class Ids(
  val trakt: IdTrakt,
  val slug: IdSlug,
  val tvdb: IdTvdb,
  val imdb: IdImdb,
  val tmdb: IdTmdb,
  val tvrage: IdTvRage
) {

  companion object {
    val EMPTY = Ids(IdTrakt(), IdSlug(), IdTvdb(), IdImdb(), IdTmdb(), IdTvRage())
  }
}

inline class IdTrakt(val id: Long = -1)

inline class IdTvdb(val id: Long = -1)

inline class IdImdb(val id: String = "")

inline class IdTmdb(val id: Long = -1)

inline class IdTvRage(val id: Long = -1)

inline class IdSlug(val id: String = "")
