package com.michaldrabik.network.trakt.model.request

import com.michaldrabik.network.trakt.model.Movie
import com.michaldrabik.network.trakt.model.Show

data class CommentRequest(
  val show: Show? = null,
  val movie: Movie? = null,
  val comment: String,
  val spoiler: Boolean
)
