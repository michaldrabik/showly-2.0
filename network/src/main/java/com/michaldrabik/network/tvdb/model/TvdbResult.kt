package com.michaldrabik.network.tvdb.model

data class TvdbResult<T>(
  val data: List<T>
)
