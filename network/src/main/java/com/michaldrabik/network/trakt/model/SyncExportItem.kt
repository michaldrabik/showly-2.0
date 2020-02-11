package com.michaldrabik.network.trakt.model

data class SyncExportItem(
  val ids: Ids
) {

  companion object {
    fun create(traktId: Long) = SyncExportItem(Ids(traktId, 0, 0, 0, "", ""))
  }
}
