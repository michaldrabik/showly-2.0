package com.michaldrabik.network.trakt.model.json

data class SyncItemJson(
  val show: ShowJson?,
  val seasons: List<SeasonJson>?
)
