package com.michaldrabik.data_remote.trakt.model

data class SyncExportRequest(
  val shows: List<SyncExportItem> = emptyList(),
  val movies: List<SyncExportItem> = emptyList(),
  val seasons: List<SyncExportItem> = emptyList(),
  val episodes: List<SyncExportItem> = emptyList()
)
