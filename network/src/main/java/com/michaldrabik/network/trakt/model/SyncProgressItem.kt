package com.michaldrabik.network.trakt.model

data class SyncProgressItem(
  val show: Show,
  val seasons: List<Season>
)