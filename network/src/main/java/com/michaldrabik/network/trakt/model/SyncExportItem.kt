package com.michaldrabik.network.trakt.model

data class SyncExportItem(
  val ids: Ids,
  val watched_at: String
) {

  companion object {
    fun create(
      traktId: Long,
      watchedAt: String = "released"
    ) = SyncExportItem(Ids(traktId, "", 0, "", 0, 0), watchedAt)
  }
}
