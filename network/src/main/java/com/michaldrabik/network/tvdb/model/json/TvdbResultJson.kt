package com.michaldrabik.network.tvdb.model.json

data class TvdbResultJson<T>(
  val data: List<T>?
)
