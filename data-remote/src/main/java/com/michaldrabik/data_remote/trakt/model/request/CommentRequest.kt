package com.michaldrabik.data_remote.trakt.model.request

import com.michaldrabik.data_remote.trakt.model.Episode
import com.michaldrabik.data_remote.trakt.model.Movie
import com.michaldrabik.data_remote.trakt.model.Show

data class CommentRequest(
  val show: Show? = null,
  val movie: Movie? = null,
  val episode: Episode? = null,
  val comment: String,
  val spoiler: Boolean
)
