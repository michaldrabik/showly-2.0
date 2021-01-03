package com.michaldrabik.network.tmdb.model

data class TmdbImages(
  val backdrops: List<TmdbImage>?,
  val posters: List<TmdbImage>?,
  val stills: List<TmdbImage>?
) {

  companion object {
    val EMPTY = TmdbImages(emptyList(), emptyList(), emptyList())
  }
}
