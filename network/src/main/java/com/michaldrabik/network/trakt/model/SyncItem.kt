package com.michaldrabik.network.trakt.model

data class SyncItem(
  val show: Show?,
  val seasons: List<Season>?
)
