package com.michaldrabik.network.trakt.model.json

data class SyncProgressItemJson(
  val show: ShowJson?,
  val seasons: List<SeasonJson>?
)