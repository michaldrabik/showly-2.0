package com.michaldrabik.network.trakt.model

data class SyncExportRequest(
  val shows: List<SyncExportShow>
)
