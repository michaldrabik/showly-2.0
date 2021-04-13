package com.michaldrabik.data_remote.trakt.model

data class SyncExportResult(
  val added: SyncExportResultItem,
  val deleted: SyncExportResultItem,
  val existing: SyncExportResultItem
)

data class SyncExportResultItem(
  val shows: Long,
  val seasons: Long,
  val episodes: Long
)
